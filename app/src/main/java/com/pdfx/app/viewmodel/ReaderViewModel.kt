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
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

private const val TAG = "ReaderViewModel"
private const val TOP_BAR_HIDE_DELAY_MS = 3_000L
private const val READING_POSITION_SAVE_DEBOUNCE_MS = 500L
private const val PAGE_RENDER_TIMEOUT_MS = 30_000L  // BUG #14: 30s timeout
private const val MAX_PAGE_CACHE_SIZE = 5            // BUG #11: limit cache size

@HiltViewModel
class ReaderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfRepository: PdfRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var pdfRenderer: PdfRenderer? = null
    private var fileDescriptor: android.os.ParcelFileDescriptor? = null

    private var topBarJob: Job? = null
    private var savePositionJob: Job? = null

    // ── Initialisation ────────────────────────────────────────────────────────

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

    fun openTemporary(uri: Uri) {
        viewModelScope.launch {
            // BUG #HIGH-07 FIX: External PDFs from intents also need URI permission persisted
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                Log.d(TAG, "Could not persist temp URI permission (expected for some providers): ${e.message}")
            }
            openUri(
                uri = uri,
                title = uri.lastPathSegment
                    ?.removeSuffix(".pdf")
                    ?.removePrefix("/") ?: "PDF",
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

                // BUG #10 FIX: Verify URI is accessible before opening
                val canAccess = try {
                    context.contentResolver.query(
                        uri, arrayOf("_id"), null, null, null
                    )?.use { it.count >= 0 } ?: false
                } catch (e: Exception) {
                    Log.w(TAG, "Cannot query URI, trying direct open: ${e.message}")
                    true // still try to open — some URIs don't support query
                }

                if (!canAccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Cannot access file.\n\nGo to Settings → Apps → PDFX → Permissions and grant Files & Media access, then try again."
                        )
                    }
                    return@withContext
                }

                val pfd = try {
                    context.contentResolver.openFileDescriptor(uri, "r")
                } catch (e: SecurityException) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Permission denied.\n\nGo to Settings → Apps → PDFX → Permissions and grant Files & Media access."
                        )
                    }
                    return@withContext
                }

                if (pfd == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Cannot open file.\n\nThe file may have been moved or deleted."
                        )
                    }
                    return@withContext
                }

                fileDescriptor = pfd

                // BUG #12 FIX: Separate IOException for corrupted PDFs
                val renderer = try {
                    PdfRenderer(pfd)
                } catch (e: java.io.IOException) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "This file is not a valid PDF or is corrupted.\n\n${e.message}"
                        )
                    }
                    return@withContext
                }

                pdfRenderer = renderer
                val pageCount = renderer.pageCount

                if (pageCount == 0) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "This PDF has no pages."
                        )
                    }
                    return@withContext
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        title = title,
                        documentId = documentId,
                        uri = uri,
                        pageCount = pageCount,
                        currentPage = startPage.coerceIn(0, pageCount - 1),
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

            } catch (e: java.io.IOException) {
                Log.e(TAG, "IO error opening PDF", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "File is corrupted or not a valid PDF.\n${e.message}"
                    )
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "Security error opening PDF", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Permission denied. Please grant file access in Settings."
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to open PDF", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to open PDF: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    // ── Page rendering ────────────────────────────────────────────────────────

    // BUG #CRITICAL-01 FIX: PdfRenderer is NOT thread-safe.
    // Use a dedicated Mutex to prevent concurrent page renders crashing the app.
    // BUG #11 FIX: LRU-style bounded cache to prevent memory leaks
    private val rendererMutex = kotlinx.coroutines.sync.Mutex()
    private val pageCacheKeys = ArrayDeque<Int>()
    private val pageCache = mutableMapOf<Int, Bitmap>()
    private var cachedTargetWidth: Int = 0

    suspend fun renderPage(pageIndex: Int, targetWidth: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            val renderer = pdfRenderer ?: return@withContext null
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return@withContext null

            if (cachedTargetWidth != targetWidth && cachedTargetWidth != 0) {
                clearPageCache()
            }
            cachedTargetWidth = targetWidth

            // Check cache before acquiring lock
            pageCache[pageIndex]?.let { cached ->
                if (!cached.isRecycled) return@withContext cached
            }

            // BUG #CRITICAL-01: All PdfRenderer operations under mutex
            rendererMutex.withLock {
                // Re-check cache inside lock
                pageCache[pageIndex]?.let { cached ->
                    if (!cached.isRecycled) return@withLock cached
                }

                // BUG #14 FIX: Timeout to prevent infinite hang on bad PDFs
                withTimeoutOrNull(PAGE_RENDER_TIMEOUT_MS) {
                    try {
                        renderer.openPage(pageIndex).use { page ->
                            val aspectRatio = page.height.toFloat() / page.width.toFloat()
                            val bitmapHeight = (targetWidth * aspectRatio).toInt().coerceAtLeast(1)

                            val bitmap = Bitmap.createBitmap(
                                targetWidth, bitmapHeight, Bitmap.Config.RGB_565
                            )
                            bitmap.eraseColor(android.graphics.Color.WHITE)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                            // BUG #11 FIX: Evict oldest entry when cache is full
                            if (pageCache.size >= MAX_PAGE_CACHE_SIZE) {
                                val oldest = pageCacheKeys.removeFirstOrNull()
                                if (oldest != null) {
                                    pageCache.remove(oldest)?.let {
                                        if (!it.isRecycled) it.recycle()
                                    }
                                }
                            }
                            pageCache[pageIndex] = bitmap
                            pageCacheKeys.remove(pageIndex)
                            pageCacheKeys.addLast(pageIndex)
                            bitmap
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to render page $pageIndex", e)
                        null
                    }
                }
            }
        }

    // BUG #11 FIX: Properly recycle all bitmaps on clear
    private fun clearPageCache() {
        pageCache.values.forEach { bmp ->
            if (!bmp.isRecycled) bmp.recycle()
        }
        pageCache.clear()
        pageCacheKeys.clear()
    }

    // BUG #11 FIX: Clear cache on low memory
    fun onLowMemory() {
        val current = _uiState.value.currentPage
        val keysToRemove = pageCache.keys.filter { it != current }
        keysToRemove.forEach { key ->
            pageCache.remove(key)?.let { if (!it.isRecycled) it.recycle() }
            pageCacheKeys.remove(key)
        }
        Log.w(TAG, "Low memory — cleared ${keysToRemove.size} cached pages")
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

    // ── Add to library ────────────────────────────────────────────────────────

    fun addToLibrary() {
        if (_uiState.value.uri == null) return
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

    // BUG #11 FIX: Always clean up on ViewModel destruction
    override fun onCleared() {
        super.onCleared()
        topBarJob?.cancel()
        savePositionJob?.cancel()
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
