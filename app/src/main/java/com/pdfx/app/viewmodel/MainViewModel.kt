package com.pdfx.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfx.app.domain.model.AccentColor
import com.pdfx.app.domain.model.AppTheme
import com.pdfx.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Lightweight ViewModel attached to MainActivity.
 * Exposes only the fields that PdfxTheme needs so the entire colour scheme
 * reacts in real-time when the user changes Accent Colour or App Theme.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val appTheme = settingsRepository.settings
        .map { it.appTheme }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppTheme.SYSTEM)

    val accentColor = settingsRepository.settings
        .map { it.accentColor }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccentColor.BLUE)
}
