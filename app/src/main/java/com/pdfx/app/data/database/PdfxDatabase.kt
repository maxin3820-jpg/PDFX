package com.pdfx.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Single Room database instance for PDFX.
 * Migrations are handled explicitly to preserve user data on upgrades.
 */
@Database(
    entities = [PdfEntity::class],
    version = 1,
    exportSchema = true
)
abstract class PdfxDatabase : RoomDatabase() {
    abstract fun pdfDao(): PdfDao

    companion object {
        const val DATABASE_NAME = "pdfx_database"
    }
}
