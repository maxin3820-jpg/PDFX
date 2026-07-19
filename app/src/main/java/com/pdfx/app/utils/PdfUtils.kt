package com.pdfx.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "PdfUtils"

/**
 * Utility functions for working with PDF files via Android's
 * built-in [PdfRenderer] API (no third-party native library required).
 */
object PdfUtils {

    /**
     * Opens a PDF and reads its page count and optional title metadata.
     * Returns null if the file cannot be opened or is not a valid PDF.
     */
    suspend fun readPdfMetadata(context: Context, uri: Uri): PdfMetadata? =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    PdfRenderer(pfd).use { renderer ->
                        PdfMetadata(pageCount = renderer.pageCount)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to read PDF metadata for $uri", e)
                null
            }
        }

    /**
     * Renders the first page of the PDF to a [Bitmap] suitable for use
     * as a library card thumbnail.
     *
     * @param targetWidth  Desired pixel width for the thumbnail.
     * 
     * PERFORMANCE: Uses RGB_565 for 50% memory savings (thumbnails don't need alpha channel).
     */
    suspend fun renderThumbnail(
        context: Context,
        uri: Uri,
        targetWidth: Int = 300
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    if (renderer.pageCount == 0) return@withContext null

                    renderer.openPage(0).use { page ->
                        val aspectRatio = page.height.toFloat() / page.width.toFloat()
                        val bitmapWidth = targetWidth
                        // BUG #NEW-05 FIX: bitmapHeight could be 0 for malformed PDFs
                        // causing Bitmap.createBitmap() to throw IllegalArgumentException
                        val bitmapHeight = (targetWidth * aspectRatio).toInt().coerceAtLeast(1)

                        // Use RGB_565 for 50% memory savings (thumbnails don't need alpha)
                        val bitmap = Bitmap.createBitmap(
                            bitmapWidth,
                            bitmapHeight,
                            Bitmap.Config.RGB_565
                        )
                        // White background for PDF pages that have transparency
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        bitmap
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to render thumbnail for $uri", e)
            null
        }
    }

    /**
     * Queries the [ContentResolver] for the display name and file size of a URI.
     * Falls back to parsing the URI path if the cursor returns nothing.
     */
    suspend fun queryFileInfo(context: Context, uri: Uri): FileInfo =
        withContext(Dispatchers.IO) {
            var name = uri.lastPathSegment ?: "document.pdf"
            var size = 0L

            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIdx >= 0) name = cursor.getString(nameIdx) ?: name
                        if (sizeIdx >= 0) size = cursor.getLong(sizeIdx)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to query file info for $uri", e)
            }

            FileInfo(
                // Bug #8 fix: removeSuffix is case-sensitive — "report.PDF" was
                // left with its extension. Use a case-insensitive check instead.
                displayName = if (name.endsWith(".pdf", ignoreCase = true))
                    name.dropLast(4).trim()
                else
                    name.trim(),
                fileName = name,
                fileSize = size,
            )
        }

    data class PdfMetadata(val pageCount: Int)
    data class FileInfo(val displayName: String, val fileName: String, val fileSize: Long)
}
