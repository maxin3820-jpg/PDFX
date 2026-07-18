package com.pdfx.app.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfx.app.domain.model.AppSettings
import com.pdfx.app.domain.model.GridLayout
import com.pdfx.app.domain.model.PdfDocument
import com.pdfx.app.domain.model.SortOrder
import com.pdfx.app.domain.repository.PdfRepository
import com.pdfx.app.domain.repository.SettingsRepository
import com.pdfx.app.utils.PdfUtils
import com.pdfx.app.utils.ThumbnailCache
import com.pdfx.app.utils.UriPermissionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfRepository: PdfRepository,
    private val settingsRepository: SettingsRepository,
    thumbnailCacheProvider: ThumbnailCache,
) : ViewModel() {

    // ── Thumbnail cache (exposed so PdfCard composables can use the same instance) ─
    val thumbnailCache: ThumbnailCache = thumbnailCacheProvider

    // ── UI State ───────────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Documents list reacts automatically to sort order changes. */
    val documents: StateFlow<List<PdfDocument>> =
        settingsRepository.settings
            .flatMapLatest { settings ->
                pdfRepository.getAllDocuments(settings.sortOrder)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    init {
        pruneStaleDocuments()
    }

    // ── Import ─────────────────────────────────────────────────────────────────

    /**
     * Imports one or more PDF URIs selected via the SAF file picker.
     * Handles duplicate detection and metadata extraction.
     *
     * Fix (Bug #6): Duplicates are now queued as a list so every duplicate
     * in a multi-file import gets its own dialog, presented one at a time.
     */
    fun importPdfs(uris: List<Uri>) {
        viewModelScope.launch {
            uris.forEach { uri ->
                try {
                    // Persist read permission immediately — must happen before any IO
                    UriPermissionHelper.persist(context, uri)

                    val uriString = uri.toString()
                    val existing = pdfRepository.getDocumentByUri(uriString)
                    if (existing != null) {
                        // Enqueue duplicate — don't clobber an existing pending event
                        _uiState.update { state ->
                            state.copy(
                                pendingDuplicates = state.pendingDuplicates +
                                    DuplicateEvent(uri = uri, existingDocument = existing)
                            )
                        }
                        return@forEach
                    }

                    doInsertPdf(uri)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to import $uri", e)
                    _uiState.update { it.copy(errorMessage = "Import failed: ${e.localizedMessage}") }
                }
            }
        }
    }

    /** The currently-shown duplicate event (head of the queue). */
    val currentDuplicateEvent: DuplicateEvent?
        get() = _uiState.value.pendingDuplicates.firstOrNull()

    /** Called when user taps "Import again" on the duplicate dialog. */
    fun forceImportDuplicate(uri: Uri) {
        viewModelScope.launch {
            // Pop the head before insert so the next duplicate can show
            _uiState.update { it.copy(pendingDuplicates = it.pendingDuplicates.drop(1)) }
            doInsertPdf(uri)
        }
    }

    /** Dismiss head duplicate event without importing. */
    fun dismissDuplicate() {
        _uiState.update { it.copy(pendingDuplicates = it.pendingDuplicates.drop(1)) }
    }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    private suspend fun doInsertPdf(uri: Uri) {
        val fileInfo = PdfUtils.queryFileInfo(context, uri)
        val pdfMeta = PdfUtils.readPdfMetadata(context, uri)

        if (pdfMeta == null) {
            _uiState.update { it.copy(errorMessage = "Could not read '${fileInfo.fileName}'") }
            return
        }

        val document = PdfDocument(
            uri         = uri.toString(),
            displayName = fileInfo.displayName,
            fileName    = fileInfo.fileName,
            fileSize    = fileInfo.fileSize,
            pageCount   = pdfMeta.pageCount,
            importedAt  = System.currentTimeMillis(),
        )

        val insertedId = pdfRepository.insertDocument(document)

        // Generate thumbnail asynchronously — doesn't block the import confirmation
        viewModelScope.launch {
            thumbnailCache.getOrGenerate(context, insertedId, uri)
        }
    }

    // ── Rename ─────────────────────────────────────────────────────────────────

    fun showRenameDialog(document: PdfDocument) =
        _uiState.update { it.copy(renameTarget = document) }

    fun dismissRenameDialog() =
        _uiState.update { it.copy(renameTarget = null) }

    fun renameDocument(id: Long, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            pdfRepository.renameDocument(id, newName.trim())
            _uiState.update { it.copy(renameTarget = null) }
        }
    }

    // ── Remove ─────────────────────────────────────────────────────────────────

    fun removeDocument(document: PdfDocument) {
        viewModelScope.launch {
            pdfRepository.removeDocument(document.id)
            thumbnailCache.evict(context, document.id)
            // Release the SAF permission; we no longer need it
            UriPermissionHelper.release(context, Uri.parse(document.uri))
        }
    }

    // ── Sort / layout (delegated to Settings) ──────────────────────────────────

    fun setSortOrder(order: SortOrder) =
        viewModelScope.launch { settingsRepository.setSortOrder(order) }

    fun setGridLayout(layout: GridLayout) =
        viewModelScope.launch { settingsRepository.setGridLayout(layout) }

    // ── Stale document pruning ─────────────────────────────────────────────────

    /**
     * Removes library entries whose SAF permission is no longer valid
     * (file moved or deleted outside PDFX). Runs silently at startup.
     *
     * Fix (Bug #1): The isNotEmpty() guard was wrong — if ALL permissions are
     * gone (fresh install / all revoked), that is exactly when we should prune
     * everything. The empty-set safety guard lives in PdfRepositoryImpl, not here.
     */
    private fun pruneStaleDocuments() {
        viewModelScope.launch {
            val validUris = UriPermissionHelper.getAllAccessibleUris(context)
            pdfRepository.pruneStaleDocuments(validUris)
        }
    }
}

// ── UI State model ─────────────────────────────────────────────────────────────

data class HomeUiState(
    /**
     * Queue of pending duplicate events — shown one at a time.
     * Fix (Bug #6): was a single nullable field; now a list so all duplicates
     * in a multi-file import are surfaced rather than silently dropped.
     */
    val pendingDuplicates: List<DuplicateEvent> = emptyList(),
    val renameTarget: PdfDocument? = null,
    val errorMessage: String? = null,
)

data class DuplicateEvent(
    val uri: Uri,
    val existingDocument: PdfDocument,
)
