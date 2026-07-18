package com.pdfx.app.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a PDF document in the local library.
 *
 * The [uri] column has a UNIQUE index — two identical file URIs cannot coexist.
 */
@Entity(
    tableName = "pdf_documents",
    indices = [Index(value = ["uri"], unique = true)]
)
data class PdfEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "uri")
    val uri: String,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    @ColumnInfo(name = "file_name")
    val fileName: String,

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    @ColumnInfo(name = "page_count")
    val pageCount: Int,

    @ColumnInfo(name = "imported_at")
    val importedAt: Long,

    @ColumnInfo(name = "last_page_index")
    val lastPageIndex: Int = 0,

    @ColumnInfo(name = "last_zoom")
    val lastZoom: Float = 1f,

    @ColumnInfo(name = "last_scroll_offset")
    val lastScrollOffset: Float = 0f,
)
