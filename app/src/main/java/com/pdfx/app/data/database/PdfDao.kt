package com.pdfx.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for all PDF library operations.
 * Sorting is handled here via multiple @Query variants so Room can
 * compile and optimize each SQL statement at build time.
 */
@Dao
interface PdfDao {

    // ── Queries ──────────────────────────────────────────────────────────────

    @Query("SELECT * FROM pdf_documents ORDER BY imported_at DESC")
    fun getAllByNewest(): Flow<List<PdfEntity>>

    @Query("SELECT * FROM pdf_documents ORDER BY imported_at ASC")
    fun getAllByOldest(): Flow<List<PdfEntity>>

    @Query("SELECT * FROM pdf_documents ORDER BY display_name COLLATE NOCASE ASC")
    fun getAllByNameAsc(): Flow<List<PdfEntity>>

    @Query("SELECT * FROM pdf_documents ORDER BY display_name COLLATE NOCASE DESC")
    fun getAllByNameDesc(): Flow<List<PdfEntity>>

    @Query("SELECT * FROM pdf_documents ORDER BY file_size DESC")
    fun getAllByFileSize(): Flow<List<PdfEntity>>

    @Query("SELECT * FROM pdf_documents WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PdfEntity?

    @Query("SELECT * FROM pdf_documents WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): PdfEntity?

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: PdfEntity): Long

    @Query("UPDATE pdf_documents SET display_name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM pdf_documents WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("""
        UPDATE pdf_documents
        SET last_page_index = :pageIndex,
            last_zoom = :zoom,
            last_scroll_offset = :scrollOffset
        WHERE id = :id
    """)
    suspend fun updateReadingPosition(
        id: Long,
        pageIndex: Int,
        zoom: Float,
        scrollOffset: Float
    )

    /** Removes any document whose URI is not in [validUris]. */
    @Query("DELETE FROM pdf_documents WHERE uri NOT IN (:validUris)")
    suspend fun deleteWhereUriNotIn(validUris: List<String>)

    @Query("SELECT COUNT(*) FROM pdf_documents")
    suspend fun count(): Int
}
