package com.pdfx.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.pdfx.app.data.database.PdfDao
import com.pdfx.app.data.database.PdfxDatabase
import com.pdfx.app.data.repository.PdfRepositoryImpl
import com.pdfx.app.data.repository.SettingsRepositoryImpl
import com.pdfx.app.domain.repository.PdfRepository
import com.pdfx.app.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "pdfx_settings")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PdfxDatabase =
        Room.databaseBuilder(
            context,
            PdfxDatabase::class.java,
            PdfxDatabase.DATABASE_NAME
        )
            // Bug #7 fix: handle both upgrade AND downgrade gracefully.
            // Previously only fallbackToDestructiveMigrationOnDowngrade() was set,
            // meaning a version bump would throw IllegalStateException at runtime
            // ("A migration from X to Y was required but not found").
            // For v1 → v2+ add explicit Migration objects before removing this.
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    @Singleton
    fun providePdfDao(database: PdfxDatabase): PdfDao = database.pdfDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPdfRepository(impl: PdfRepositoryImpl): PdfRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
