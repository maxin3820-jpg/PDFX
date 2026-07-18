package com.pdfx.app.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfx.app.domain.model.ReaderTheme
import com.pdfx.app.domain.repository.PdfRepository
import com.pdfx.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "ReaderViewModel"
private const val TOP_BAR_HIDE_DELAY_MS = 3_000L
private const val READING_POSITION_SAVE_DEBOUNCE_MS = 500L

@HiltViewModel
class ReaderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfRepository: PdfRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    /** PdfRenderer instance — opened once per document, closed on clear. */
    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: android.os.ParcelFileDescriptor? = null

    private var topBarJob: Job? = null
    private var savePositionJob: Job? = null

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Opens a PDF from the library by its database ID.
     * Restores reading position if the user has that setting enabled.
     */
    fun openFromLibrary(documentId: Long) {
        viewModelScope.launch {
            val doc = pdfRepository.getDocumentById(documentId) ?: run {
                _uiState.update { it.copy(error = "Document not found") }
                return@launch
            }
            val settings = settingsRepository.settings.first()
            val startPage = if (settings.rememberReadingPosition) doc.lastPageIndex else 0
            val startZoom = if (settings.rememberReadingPosition) doc.lastZoom else 1f

            openUri(
                uri = Uri.parse(doc.uri),
                title = doc.displayName,
                documentId = doc.id,
                startPage = startPage,
                startZoom = startZoom,
                readerTheme = settings.readerTheme,
                showPageNumber = settings.showPageNumber,
                keepScreenOn = settings.keepScreenOn,
                enableAnimations = settings.enableAnimations,
            )
        }
    }

    /**
     * Opens a temporary PDF from an external intent (not added to library).
     */
    fun openTemporary(uri: Uri) {
        viewModelScope.launch {
            openUri(
                uri = uri,
                title = uri.lastPathSegment?.removeSuffix(".pdf") ?: "PDF",
                documentId = null,
                startPage = 0,
                startZoom = 1f,
            )
        }
    }

    private suspend fun openUri(
        uri: Uri,
        title: String,
        documentId: Long?,
        startPage: Int,
        startZoom: Float,
        readerTheme: ReaderTheme = ReaderTheme.LIGHT,
        showPageNumber: Boolean = true,
        keepScreenOn: Boolean = false,
        enableAnimations: Boolean = true,
    ) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        withContext(Dispatchers.IO) {
            try {
                closeRenderer()
                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: run {
                    _uiState.update { it.copy(isLoading = false, error = "Cannot open file") }
                    return@withContext
                }
                fileDescriptor = pfd
                val renderer = PdfRenderer(pfd)
                pdfRenderer = renderer
                val pageCount = renderer.pageCount

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        title = title,
                        documentId = documentId,
                        uri = uri,
                        pageCount = pageCount,
                        currentPage = startPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0)),
                        zoom = startZoom,
                        readerTheme = readerTheme,
                        showPageNumber = showPageNumber,
                        keepScreenOn = keepScreenOn,
                        enableAnimations = enableAnimations,
                        isTemporary = documentId == null,
                        topBarVisible = true,
                    )
                }

                scheduleTopBarHide()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open PDF", e)
                _uiState.update { it.copy(isLoading = false, error = "Failed to open PDF: ${e.localizedMessage}") }
            }
        }
    }

    // ── Page rendering ────────────────────────────────────────────────────────

    // ── Page rendering with caching ───────────────────────────────────────────

    /** Cache for rendered pages — keyed by pageIndex. Cleared on document switch. */
    private val pageCache = mutableMapOf<Int, Bitmap>()
    private var cachedTargetWidth: Int = 0

    /**
     * Renders a single PDF page to a [Bitmap] at the specified width.
     * This is called per-page by the lazy column in the reader.
     * 
     * PERFORMANCE OPTIMIZATIONS:
     * 1. Page cache — rendered bitmaps are cached to avoid redundant rendering
     * 2. RGB_565 config — uses 50% less memory than ARGB_8888 for PDFs without transparency
     * 3. RENDER_MODE_FOR_DISPLAY — optimized for screen display (vs print quality)
     */
    suspend fun renderPage(pageIndex: Int, targetWidth: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            val renderer = pdfRenderer ?: return@withContext null
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

            // Clear cache if target width changed (orientation rotation)
            if (cachedTargetWidth != targetWidth && cachedTargetWidth != 0) {
                clearPageCache()
            }
            cachedTargetWidth = targetWidth

            // Return cached bitmap if available
            pageCache[pageIndex]?.let { return@withContext it }

            try {
                renderer.openPage(pageIndex).use { page ->
                    val aspectRatio = page.height.toFloat() / page.width.toFloat()
                    val bitmapWidth = targetWidth
                    val bitmapHeight = (targetWidth * aspectRatio).toInt()

                    // Use RGB_565 for 50% memory savings (sufficient for most PDFs)
                    val bitmap = Bitmap.createBitmap(
                        bitmapWidth, bitmapHeight, Bitmap.Config.RGB_565
                    )
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    
                    // Cache the rendered page
                    pageCache[pageIndex] = bitmap
                    bitmap
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to render page $pageIndex", e)
                null
            }
        }

    private fun clearPageCache() {
        pageCache.values.forEach { it.recycle() }
        pageCache.clear()
    }

    // ── Reading position ──────────────────────────────────────────────────────

    fun onPageChanged(pageIndex: Int) {
        _uiState.update { it.copy(currentPage = pageIndex) }
        saveReadingPositionDebounced()
    }

    fun onZoomChanged(zoom: Float) {
        _uiState.update { it.copy(zoom = zoom) }
        saveReadingPositionDebounced()
    }

    fun onScrollOffsetChanged(offset: Float) {
        _uiState.update { it.copy(scrollOffset = offset) }
        saveReadingPositionDebounced()
    }

    private fun saveReadingPositionDebounced() {
        savePositionJob?.cancel()
        savePositionJob = viewModelScope.launch {
            delay(READING_POSITION_SAVE_DEBOUNCE_MS)
            val state = _uiState.value
            val docId = state.documentId ?: return@launch

            val settings = settingsRepository.settings.first()
            if (!settings.rememberReadingPosition) return@launch

            pdfRepository.updateReadingPosition(
                id = docId,
                pageIndex = state.currentPage,
                zoom = state.zoom,
                scrollOffset = state.scrollOffset,
            )
        }
    }

    // ── Top bar visibility ────────────────────────────────────────────────────

    fun onTap() {
        _uiState.update { it.copy(topBarVisible = true) }
        scheduleTopBarHide()
    }

    private fun scheduleTopBarHide() {
        topBarJob?.cancel()
        topBarJob = viewModelScope.launch {
            delay(TOP_BAR_HIDE_DELAY_MS)
            _uiState.update { it.copy(topBarVisible = false) }
        }
    }

    // ── Add to library (temporary opens) ─────────────────────────────────────

    /**
     * Signals that the user wants to add the currently-open temporary PDF to
     * the library. The actual insert is performed by HomeViewModel via the
     * onAddToLibrary callback wired in PdfxNavGraph → MainActivity.
     *
     * Bug #3 fix: removed the dead `uri` local variable (was validated but
     * never used inside the coroutine) and the misleading try/catch that wrapped
     * code that could never throw. The flag is now set directly and synchronously;
     * ReaderScreen's LaunchedEffect observes it and invokes the callback.
     */
    fun addToLibrary() {
        if (_uiState.value.uri == null) return   // nothing open — no-op
        _uiState.update { it.copy(addToLibraryRequested = true) }
    }

    fun clearAddToLibraryRequest() {
        _uiState.update { it.copy(addToLibraryRequested = false) }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    private fun closeRenderer() {
        try {
            clearPageCache()
            pdfRenderer?.close()
            fileDescriptor?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing renderer", e)
        } finally {
            pdfRenderer = null
            fileDescriptor = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        closeRenderer()
    }
}

// ── UI State ──────────────────────────────────────────────────────────────────

data class ReaderUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val title: String = "",
    val documentId: Long? = null,
    val uri: Uri? = null,
    val pageCount: Int = 0,
    val currentPage: Int = 0,
    val zoom: Float = 1f,
    val scrollOffset: Float = 0f,
    val topBarVisible: Boolean = true,
    val isTemporary: Boolean = false,
    val addToLibraryRequested: Boolean = false,
    val readerTheme: ReaderTheme = ReaderTheme.LIGHT,
    val showPageNumber: Boolean = true,
    val keepScreenOn: Boolean = false,
    val enableAnimations: Boolean = true,
)
