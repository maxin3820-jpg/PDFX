package com.pdfx.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    fun setAppTheme(theme: AppTheme) =
        viewModelScope.launch { settingsRepository.setAppTheme(theme) }

    fun setAccentColor(color: AccentColor) =
        viewModelScope.launch { settingsRepository.setAccentColor(color) }

    fun setReaderTheme(theme: ReaderTheme) =
        viewModelScope.launch { settingsRepository.setReaderTheme(theme) }

    fun setReaderBackground(bg: ReaderBackground) =
        viewModelScope.launch { settingsRepository.setReaderBackground(bg) }

    fun setCardStyle(style: CardStyle) =
        viewModelScope.launch { settingsRepository.setCardStyle(style) }

    fun setRememberReadingPosition(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setRememberReadingPosition(enabled) }

    fun setDefaultZoom(zoom: DefaultZoom) =
        viewModelScope.launch { settingsRepository.setDefaultZoom(zoom) }

    fun setGridLayout(layout: GridLayout) =
        viewModelScope.launch { settingsRepository.setGridLayout(layout) }

    fun setSortOrder(order: SortOrder) =
        viewModelScope.launch { settingsRepository.setSortOrder(order) }

    fun setKeepScreenOn(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setKeepScreenOn(enabled) }

    fun setEnableAnimations(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setEnableAnimations(enabled) }

    fun setShowPageNumber(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setShowPageNumber(enabled) }
}
