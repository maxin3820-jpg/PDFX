package com.pdfx.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A labelled swatch grid row used in Settings to pick accent colour or
 * reader background. Each swatch is a coloured circle with an optional
 * label below it and a check-mark overlay when selected.
 */
@Composable
fun ColorSwatchRow(
    title: String,
    swatches: List<SwatchItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SettingsSectionHeader(title)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 56.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                // Grid is non-scrollable; height wraps content via fixed row count.
                // Use a fixed height that fits all swatches (max 12 items in 2 rows).
                .padding(bottom = 12.dp),
            // Non-scrollable grid — height is set by content
            userScrollEnabled = false,
        ) {
            items(swatches, key = { it.key }) { swatch ->
                ColorSwatch(
                    swatch = swatch,
                    selected = swatch.key == selectedKey,
                    onClick = { onSelect(swatch.key) },
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    swatch: SwatchItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(swatch.color)
                    .then(
                        if (selected) Modifier.border(
                            width = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ) else Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            shape = CircleShape,
                        )
                    ),
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (swatch.isDark) Color.White else Color(0xFF1A1A1A),
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        if (swatch.label != null) {
            Text(
                text = swatch.label,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
            )
        }
    }
}

data class SwatchItem(
    val key: String,
    val color: Color,
    val label: String? = null,
    val isDark: Boolean = false,
)
