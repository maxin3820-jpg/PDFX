package com.pdfx.app.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdfx.app.BuildConfig
import com.pdfx.app.domain.model.AccentColor
import com.pdfx.app.domain.model.AppTheme
import com.pdfx.app.domain.model.CardStyle
import com.pdfx.app.domain.model.DefaultZoom
import com.pdfx.app.domain.model.GridLayout
import com.pdfx.app.domain.model.ReaderBackground
import com.pdfx.app.domain.model.ReaderTheme
import com.pdfx.app.domain.model.SortOrder
import com.pdfx.app.ui.components.CardStylePickerRow
import com.pdfx.app.ui.components.ClickableSettingsRow
import com.pdfx.app.ui.components.ColorSwatchRow
import com.pdfx.app.ui.components.SettingsSectionHeader
import com.pdfx.app.ui.components.SwatchItem
import com.pdfx.app.ui.components.SwitchSettingsRow
import com.pdfx.app.ui.theme.AccentPalettes
import com.pdfx.app.ui.theme.ReaderBackgrounds
import com.pdfx.app.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {

            // ══════════════════════════════════════════════════════
            // APPEARANCE
            // ══════════════════════════════════════════════════════
            item { SettingsSectionHeader("Appearance") }

            item {
                OptionSettingsRow(
                    title = "App Theme",
                    currentValue = when (settings.appTheme) {
                        AppTheme.SYSTEM -> "System default"
                        AppTheme.LIGHT  -> "Light"
                        AppTheme.DARK   -> "Dark"
                    },
                    options = listOf("System default", "Light", "Dark"),
                    onOptionSelected = {
                        viewModel.setAppTheme(
                            when (it) {
                                "Light" -> AppTheme.LIGHT
                                "Dark"  -> AppTheme.DARK
                                else    -> AppTheme.SYSTEM
                            }
                        )
                    },
                )
            }

            // Accent colour — 10 swatches
            item {
                val accentSwatches = AccentColor.entries.map { ac ->
                    val palette = AccentPalettes[ac]!!
                    SwatchItem(
                        key = ac.value,
                        color = palette.light,
                        label = palette.name,
                        isDark = false,
                    )
                }
                ColorSwatchRow(
                    title = "Accent Colour",
                    swatches = accentSwatches,
                    selectedKey = settings.accentColor.value,
                    onSelect = { key ->
                        AccentColor.entries.firstOrNull { it.value == key }
                            ?.let { viewModel.setAccentColor(it) }
                    },
                )
            }

            item { HorizontalDivider() }

            // ══════════════════════════════════════════════════════
            // READER
            // ══════════════════════════════════════════════════════
            item { SettingsSectionHeader("Reader") }

            item {
                OptionSettingsRow(
                    title = "Reader Theme",
                    currentValue = when (settings.readerTheme) {
                        ReaderTheme.LIGHT -> "Light"
                        ReaderTheme.DARK  -> "Dark"
                        ReaderTheme.SEPIA -> "Sepia"
                    },
                    options = listOf("Light", "Dark", "Sepia"),
                    onOptionSelected = {
                        viewModel.setReaderTheme(
                            when (it) {
                                "Dark"  -> ReaderTheme.DARK
                                "Sepia" -> ReaderTheme.SEPIA
                                else    -> ReaderTheme.LIGHT
                            }
                        )
                    },
                )
            }

            // Reader background — 12 swatches
            item {
                val bgSwatches = ReaderBackground.entries.map { rb ->
                    val palette = ReaderBackgrounds[rb]!!
                    SwatchItem(
                        key = rb.value,
                        color = palette.background,
                        label = palette.name,
                        isDark = palette.isDark,
                    )
                }
                ColorSwatchRow(
                    title = "Page Background",
                    swatches = bgSwatches,
                    selectedKey = settings.readerBackground.value,
                    onSelect = { key ->
                        ReaderBackground.entries.firstOrNull { it.value == key }
                            ?.let { viewModel.setReaderBackground(it) }
                    },
                )
            }

            item {
                SwitchSettingsRow(
                    title = "Remember Reading Position",
                    subtitle = "Restore page, zoom, and scroll on reopen",
                    checked = settings.rememberReadingPosition,
                    onCheckedChange = viewModel::setRememberReadingPosition,
                )
            }

            item {
                OptionSettingsRow(
                    title = "Default Zoom",
                    currentValue = when (settings.defaultZoom) {
                        DefaultZoom.FIT_WIDTH       -> "Fit Width"
                        DefaultZoom.FIT_HEIGHT      -> "Fit Height"
                        DefaultZoom.HUNDRED_PERCENT -> "100%"
                    },
                    options = listOf("Fit Width", "Fit Height", "100%"),
                    onOptionSelected = {
                        viewModel.setDefaultZoom(
                            when (it) {
                                "Fit Height" -> DefaultZoom.FIT_HEIGHT
                                "100%"       -> DefaultZoom.HUNDRED_PERCENT
                                else         -> DefaultZoom.FIT_WIDTH
                            }
                        )
                    },
                )
            }

            item {
                SwitchSettingsRow(
                    title = "Keep Screen On",
                    subtitle = "Prevent screen from sleeping while reading",
                    checked = settings.keepScreenOn,
                    onCheckedChange = viewModel::setKeepScreenOn,
                )
            }

            item {
                SwitchSettingsRow(
                    title = "Show Page Number",
                    checked = settings.showPageNumber,
                    onCheckedChange = viewModel::setShowPageNumber,
                )
            }

            item {
                SwitchSettingsRow(
                    title = "Enable Animations",
                    checked = settings.enableAnimations,
                    onCheckedChange = viewModel::setEnableAnimations,
                )
            }

            item { HorizontalDivider() }

            // ══════════════════════════════════════════════════════
            // LIBRARY
            // ══════════════════════════════════════════════════════
            item { SettingsSectionHeader("Library") }

            // Card style preview picker
            item {
                CardStylePickerRow(
                    selectedStyle = settings.cardStyle,
                    onSelect = { viewModel.setCardStyle(it) },
                )
            }

            item {
                OptionSettingsRow(
                    title = "Grid Layout",
                    currentValue = when (settings.gridLayout) {
                        GridLayout.TWO_COLUMN    -> "Two Columns"
                        GridLayout.SINGLE_COLUMN -> "Single Column"
                    },
                    options = listOf("Two Columns", "Single Column"),
                    onOptionSelected = {
                        viewModel.setGridLayout(
                            if (it == "Single Column") GridLayout.SINGLE_COLUMN
                            else GridLayout.TWO_COLUMN
                        )
                    },
                )
            }

            item {
                OptionSettingsRow(
                    title = "Sort By",
                    currentValue = when (settings.sortOrder) {
                        SortOrder.NEWEST    -> "Newest First"
                        SortOrder.OLDEST    -> "Oldest First"
                        SortOrder.A_TO_Z    -> "A → Z"
                        SortOrder.Z_TO_A    -> "Z → A"
                        SortOrder.FILE_SIZE -> "File Size"
                    },
                    options = listOf("Newest First", "Oldest First", "A → Z", "Z → A", "File Size"),
                    onOptionSelected = {
                        viewModel.setSortOrder(
                            when (it) {
                                "Oldest First" -> SortOrder.OLDEST
                                "A → Z"        -> SortOrder.A_TO_Z
                                "Z → A"        -> SortOrder.Z_TO_A
                                "File Size"    -> SortOrder.FILE_SIZE
                                else           -> SortOrder.NEWEST
                            }
                        )
                    },
                )
            }

            item { HorizontalDivider() }

            // ══════════════════════════════════════════════════════
            // ABOUT
            // ══════════════════════════════════════════════════════
            item { SettingsSectionHeader("About") }

            item {
                ClickableSettingsRow(
                    title = "Privacy Policy",
                    subtitle = "No data collected. Everything stays on your device.",
                    onClick = { },
                )
            }

            item {
                ClickableSettingsRow(
                    title = "Open Source Licenses",
                    onClick = { },
                )
            }

            item {
                ClickableSettingsRow(
                    title = "Version",
                    subtitle = BuildConfig.VERSION_NAME,
                    onClick = { },
                )
            }
        }
    }
}

// ── Internal: option picker row ───────────────────────────────────────────────

@Composable
private fun OptionSettingsRow(
    title: String,
    currentValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    ClickableSettingsRow(
        title = title,
        subtitle = currentValue,
        onClick = { showDialog = true },
        modifier = modifier,
    )

    if (showDialog) {
        OptionPickerDialog(
            title = title,
            options = options,
            selectedOption = currentValue,
            onOptionSelected = {
                onOptionSelected(it)
                showDialog = false
            },
            onDismiss = { showDialog = false },
        )
    }
}
