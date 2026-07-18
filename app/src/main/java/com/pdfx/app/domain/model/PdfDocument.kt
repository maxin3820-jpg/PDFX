package com.pdfx.app.domain.model

/**
 * Domain model representing a PDF document stored in the PDFX library.
 * This is the pure domain object, decoupled from Room entities.
 */
data class PdfDocument(
    val id: Long = 0,
    /** Persistent URI granted via SAF — never changes after import */
    val uri: String,
    /** User-facing display name (may differ from filename) */
    val displayName: String,
    /** Original filename from the file system */
    val fileName: String,
    val fileSize: Long,
    val pageCount: Int,
    val importedAt: Long,
    /** Last reading position: page index (0-based) */
    val lastPageIndex: Int = 0,
    /** Last reading zoom factor */
    val lastZoom: Float = 1f,
    /** Last scroll offset within the page */
    val lastScrollOffset: Float = 0f,
)

/**
 * Formats file size from bytes to a human-readable string (e.g. "2.4 MB").
 */
fun Long.toReadableFileSize(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> "%.1f GB".format(gb)
        mb >= 1.0 -> "%.1f MB".format(mb)
        kb >= 1.0 -> "%.0f KB".format(kb)
        else -> "$this B"
    }
}
