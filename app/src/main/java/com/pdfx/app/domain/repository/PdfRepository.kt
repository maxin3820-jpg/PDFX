package com.pdfx.app.domain.repository

import com.pdfx.app.domain.model.PdfDocument
import com.pdfx.app.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for PDF document CRUD operations.
 * The UI layer depends on this interface, not on the Room implementation.
 */
interface PdfRepository {

    /** Observe all documents, reacting to sort order changes. */
    fun getAllDocuments(sortOrder: SortOrder): Flow<List<PdfDocument>>

    /** Get a single document by its ID. */
    suspend fun getDocumentById(id: Long): PdfDocument?

    /** Get a document by its URI string (used for duplicate detection). */
    suspend fun getDocumentByUri(uri: String): PdfDocument?

    /** Insert a new document and return its generated ID. */
    suspend fun insertDocument(document: PdfDocument): Long

    /** Update only the display name of an existing document. */
    suspend fun renameDocument(id: Long, newName: String)

    /** Remove a document from the library (does NOT delete the original file). */
    suspend fun removeDocument(id: Long)

    /** Persist reading progress for a document. */
    suspend fun updateReadingPosition(
        id: Long,
        pageIndex: Int,
        zoom: Float,
        scrollOffset: Float
    )

    /** Remove documents whose URIs are no longer accessible (file moved/deleted outside app). */
    suspend fun pruneStaleDocuments(validUris: Set<String>)
}
