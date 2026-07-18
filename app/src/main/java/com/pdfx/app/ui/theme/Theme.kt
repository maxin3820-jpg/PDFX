package com.pdfx.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.pdfx.app.domain.model.AccentColor
import com.pdfx.app.domain.model.AppTheme

@Composable
fun PdfxTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    accentColor: AccentColor = AccentColor.BLUE,
    content: @Composable () -> Unit,
) {
    val isDark = when (appTheme) {
        AppTheme.LIGHT  -> false
        AppTheme.DARK   -> true
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val context = LocalContext.current

    // On Android 12+ use dynamic colour (ignores custom accent — system handles it).
    // On older versions build a scheme from the chosen AccentPalette.
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (isDark) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)

        else -> {
            val palette = AccentPalettes[accentColor]
                ?: AccentPalettes[AccentColor.BLUE]!!

            if (isDark) darkColorScheme(
                primary            = palette.dark,
                onPrimary          = palette.lightDark,
                primaryContainer   = palette.container.copy(alpha = 0.25f),
                onPrimaryContainer = palette.dark.copy(alpha = 0.85f),
                secondary          = Neutral400,
                onSecondary        = Neutral900,
                background         = Neutral950,
                onBackground       = Neutral100,
                surface            = Color(0xFF1C1C1E),
                onSurface          = Neutral100,
                surfaceVariant     = Neutral800,
                onSurfaceVariant   = Neutral400,
                outline            = Neutral700,
                error              = ErrorRed,
                onError            = Color.White,
            )
            else lightColorScheme(
                primary            = palette.light,
                onPrimary          = Color.White,
                primaryContainer   = palette.container,
                onPrimaryContainer = palette.lightDark,
                secondary          = Neutral600,
                onSecondary        = Color.White,
                background         = Neutral50,
                onBackground       = Neutral900,
                surface            = Color.White,
                onSurface          = Neutral900,
                surfaceVariant     = Neutral100,
                onSurfaceVariant   = Neutral600,
                outline            = Neutral300,
                error              = ErrorRed,
                onError            = Color.White,
            )
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = PdfxTypography,
        content     = content,
    )
}
