package com.pdfx.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pdfx.app.domain.model.AccentColor
import com.pdfx.app.domain.model.AppSettings
import com.pdfx.app.domain.model.AppTheme
import com.pdfx.app.domain.model.CardStyle
import com.pdfx.app.domain.model.DefaultZoom
import com.pdfx.app.domain.model.GridLayout
import com.pdfx.app.domain.model.ReaderBackground
import com.pdfx.app.domain.model.ReaderTheme
import com.pdfx.app.domain.model.SortOrder
import com.pdfx.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private object Keys {
        val APP_THEME                 = stringPreferencesKey("app_theme")
        val ACCENT_COLOR              = stringPreferencesKey("accent_color")
        val READER_THEME              = stringPreferencesKey("reader_theme")
        val READER_BACKGROUND         = stringPreferencesKey("reader_background")
        val CARD_STYLE                = stringPreferencesKey("card_style")
        val REMEMBER_READING_POSITION = booleanPreferencesKey("remember_reading_position")
        val DEFAULT_ZOOM              = stringPreferencesKey("default_zoom")
        val GRID_LAYOUT               = stringPreferencesKey("grid_layout")
        val SORT_ORDER                = stringPreferencesKey("sort_order")
        val KEEP_SCREEN_ON            = booleanPreferencesKey("keep_screen_on")
        val ENABLE_ANIMATIONS         = booleanPreferencesKey("enable_animations")
        val SHOW_PAGE_NUMBER          = booleanPreferencesKey("show_page_number")
    }

    private val defaults = AppSettings()

    override val settings: Flow<AppSettings> = dataStore.data.map { p ->
        AppSettings(
            appTheme = AppTheme.entries.firstOrNull {
                it.value == p[Keys.APP_THEME] } ?: defaults.appTheme,
            accentColor = AccentColor.entries.firstOrNull {
                it.value == p[Keys.ACCENT_COLOR] } ?: defaults.accentColor,
            readerTheme = ReaderTheme.entries.firstOrNull {
                it.value == p[Keys.READER_THEME] } ?: defaults.readerTheme,
            readerBackground = ReaderBackground.entries.firstOrNull {
                it.value == p[Keys.READER_BACKGROUND] } ?: defaults.readerBackground,
            cardStyle = CardStyle.entries.firstOrNull {
                it.value == p[Keys.CARD_STYLE] } ?: defaults.cardStyle,
            rememberReadingPosition = p[Keys.REMEMBER_READING_POSITION]
                ?: defaults.rememberReadingPosition,
            defaultZoom = DefaultZoom.entries.firstOrNull {
                it.value == p[Keys.DEFAULT_ZOOM] } ?: defaults.defaultZoom,
            gridLayout = GridLayout.entries.firstOrNull {
                it.value == p[Keys.GRID_LAYOUT] } ?: defaults.gridLayout,
            sortOrder = SortOrder.entries.firstOrNull {
                it.value == p[Keys.SORT_ORDER] } ?: defaults.sortOrder,
            keepScreenOn     = p[Keys.KEEP_SCREEN_ON]     ?: defaults.keepScreenOn,
            enableAnimations = p[Keys.ENABLE_ANIMATIONS]  ?: defaults.enableAnimations,
            showPageNumber   = p[Keys.SHOW_PAGE_NUMBER]   ?: defaults.showPageNumber,
        )
    }

    override suspend fun setAppTheme(theme: AppTheme) =
        dataStore.edit { it[Keys.APP_THEME] = theme.value }

    override suspend fun setAccentColor(color: AccentColor) =
        dataStore.edit { it[Keys.ACCENT_COLOR] = color.value }

    override suspend fun setReaderTheme(theme: ReaderTheme) =
        dataStore.edit { it[Keys.READER_THEME] = theme.value }

    override suspend fun setReaderBackground(bg: ReaderBackground) =
        dataStore.edit { it[Keys.READER_BACKGROUND] = bg.value }

    override suspend fun setCardStyle(style: CardStyle) =
        dataStore.edit { it[Keys.CARD_STYLE] = style.value }

    override suspend fun setRememberReadingPosition(enabled: Boolean) =
        dataStore.edit { it[Keys.REMEMBER_READING_POSITION] = enabled }

    override suspend fun setDefaultZoom(zoom: DefaultZoom) =
        dataStore.edit { it[Keys.DEFAULT_ZOOM] = zoom.value }

    override suspend fun setGridLayout(layout: GridLayout) =
        dataStore.edit { it[Keys.GRID_LAYOUT] = layout.value }

    override suspend fun setSortOrder(order: SortOrder) =
        dataStore.edit { it[Keys.SORT_ORDER] = order.value }

    override suspend fun setKeepScreenOn(enabled: Boolean) =
        dataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }

    override suspend fun setEnableAnimations(enabled: Boolean) =
        dataStore.edit { it[Keys.ENABLE_ANIMATIONS] = enabled }

    override suspend fun setShowPageNumber(enabled: Boolean) =
        dataStore.edit { it[Keys.SHOW_PAGE_NUMBER] = enabled }
}
