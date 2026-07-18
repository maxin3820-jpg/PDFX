package com.pdfx.app.di

import com.pdfx.app.utils.ThumbnailCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides [ThumbnailCache] as a Hilt singleton.
 * The single instance is shared across all ViewModels that need thumbnail access.
 */
@Module
@InstallIn(SingletonComponent::class)
object CacheModule {

    @Provides
    @Singleton
    fun provideThumbnailCache(): ThumbnailCache = ThumbnailCache()
}
