package com.pdfx.app.domain.repository

import com.pdfx.app.domain.model.AccentColor
import com.pdfx.app.domain.model.AppSettings
import com.pdfx.app.domain.model.AppTheme
import com.pdfx.app.domain.model.CardStyle
import com.pdfx.app.domain.model.DefaultZoom
import com.pdfx.app.domain.model.GridLayout
import com.pdfx.app.domain.model.ReaderBackground
import com.pdfx.app.domain.model.ReaderTheme
import com.pdfx.app.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val settings: Flow<AppSettings>

    suspend fun setAppTheme(theme: AppTheme)
    suspend fun setAccentColor(color: AccentColor)
    suspend fun setReaderTheme(theme: ReaderTheme)
    suspend fun setReaderBackground(bg: ReaderBackground)
    suspend fun setCardStyle(style: CardStyle)
    suspend fun setRememberReadingPosition(enabled: Boolean)
    suspend fun setDefaultZoom(zoom: DefaultZoom)
    suspend fun setGridLayout(layout: GridLayout)
    suspend fun setSortOrder(order: SortOrder)
    suspend fun setKeepScreenOn(enabled: Boolean)
    suspend fun setEnableAnimations(enabled: Boolean)
    suspend fun setShowPageNumber(enabled: Boolean)
}
