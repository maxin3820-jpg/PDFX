package com.pdfx.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pdfx.app.domain.model.CardStyle

/**
 * Horizontal card-style preview picker for Settings.
 * Shows a small visual preview of each card style so the user can see
 * the difference before selecting.
 */
@Composable
fun CardStylePickerRow(
    selectedStyle: CardStyle,
    onSelect: (CardStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsSectionHeader("Card Style")

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            userScrollEnabled = false,
        ) {
            items(CardStyle.entries, key = { it.value }) { style ->
                CardStylePreview(
                    style = style,
                    selected = style == selectedStyle,
                    onClick = { onSelect(style) },
                )
            }
        }
    }
}

@Composable
private fun CardStylePreview(
    style: CardStyle,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    val borderWidth = if (selected) 2.dp else 1.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        // Mini card preview
        val previewModifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .clip(MaterialTheme.shapes.small)
            .border(borderWidth, borderColor, MaterialTheme.shapes.small)

        when (style) {
            CardStyle.ELEVATED -> Box(
                modifier = previewModifier
                    .shadow(3.dp, MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surface)
            ) { CardPreviewContent(light = true) }

            CardStyle.FLAT -> Box(
                modifier = previewModifier
                    .background(MaterialTheme.colorScheme.surface)
            ) { CardPreviewContent(light = true) }

            CardStyle.FILLED -> Box(
                modifier = previewModifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) { CardPreviewContent(light = true) }

            CardStyle.OUTLINED -> Box(
                modifier = previewModifier
                    .background(Color.White)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), MaterialTheme.shapes.small)
            ) { CardPreviewContent(light = true) }

            CardStyle.COMPACT -> Box(
                modifier = previewModifier
                    .background(MaterialTheme.colorScheme.surface)
            ) { CardPreviewContentCompact() }
        }

        Text(
            text = style.displayName,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CardPreviewContent(light: Boolean) {
    val lineColor = if (light) Color(0xFFE0E0E0) else Color(0xFF404040)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.4f).background(lineColor.copy(alpha = 0.5f)).clip(MaterialTheme.shapes.extraSmall))
        Box(Modifier.fillMaxWidth(0.8f).padding(top = 3.dp).background(lineColor).clip(MaterialTheme.shapes.extraSmall).padding(vertical = 2.dp))
        Box(Modifier.fillMaxWidth(0.5f).background(lineColor.copy(0.6f)).clip(MaterialTheme.shapes.extraSmall).padding(vertical = 1.5.dp))
    }
}

@Composable
private fun CardPreviewContentCompact() {
    val lineColor = Color(0xFFE0E0E0)
    Column(
        modifier = Modifier.fillMaxWidth().padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        repeat(3) {
            Box(
                Modifier.fillMaxWidth().background(lineColor).clip(MaterialTheme.shapes.extraSmall).padding(vertical = 3.dp)
            )
        }
    }
}

val CardStyle.displayName: String get() = when (this) {
    CardStyle.ELEVATED -> "Elevated"
    CardStyle.FLAT     -> "Flat"
    CardStyle.FILLED   -> "Filled"
    CardStyle.OUTLINED -> "Outlined"
    CardStyle.COMPACT  -> "Compact"
}
