package com.pdfx.app.data.repository

import com.pdfx.app.data.database.PdfDao
import com.pdfx.app.data.database.toDomain
import com.pdfx.app.data.database.toEntity
import com.pdfx.app.domain.model.PdfDocument
import com.pdfx.app.domain.model.SortOrder
import com.pdfx.app.domain.repository.PdfRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of [PdfRepository].
 * All IO operations run on the coroutine dispatcher supplied by the DAO.
 */
@Singleton
class PdfRepositoryImpl @Inject constructor(
    private val dao: PdfDao
) : PdfRepository {

    override fun getAllDocuments(sortOrder: SortOrder): Flow<List<PdfDocument>> {
        val rawFlow = when (sortOrder) {
            SortOrder.NEWEST    -> dao.getAllByNewest()
            SortOrder.OLDEST    -> dao.getAllByOldest()
            SortOrder.A_TO_Z    -> dao.getAllByNameAsc()
            SortOrder.Z_TO_A    -> dao.getAllByNameDesc()
            SortOrder.FILE_SIZE -> dao.getAllByFileSize()
        }
        return rawFlow.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getDocumentById(id: Long): PdfDocument? =
        dao.getById(id)?.toDomain()

    override suspend fun getDocumentByUri(uri: String): PdfDocument? =
        dao.getByUri(uri)?.toDomain()

    override suspend fun insertDocument(document: PdfDocument): Long =
        dao.insert(document.toEntity())

    override suspend fun renameDocument(id: Long, newName: String) =
        dao.rename(id, newName)

    override suspend fun removeDocument(id: Long) =
        dao.delete(id)

    override suspend fun updateReadingPosition(
        id: Long,
        pageIndex: Int,
        zoom: Float,
        scrollOffset: Float
    ) = dao.updateReadingPosition(id, pageIndex, zoom, scrollOffset)

    override suspend fun pruneStaleDocuments(validUris: Set<String>) {
        // BUG #CRITICAL-04 FIX: Empty set passed to NOT IN() deletes ALL rows in SQLite.
        // Always guard against empty set - if no valid URIs exist don't wipe everything.
        if (validUris.isEmpty()) return
        dao.deleteWhereUriNotIn(validUris.toList())
    }
}
