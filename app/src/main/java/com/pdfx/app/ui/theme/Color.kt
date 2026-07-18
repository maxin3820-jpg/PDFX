package com.pdfx.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.pdfx.app.domain.model.AccentColor
import com.pdfx.app.domain.model.ReaderBackground

// ── Brand defaults ─────────────────────────────────────────────────────────────
val PdfxBlue       = Color(0xFF2563EB)
val PdfxBlueDark   = Color(0xFF1D4ED8)
val PdfxBlueLight  = Color(0xFF60A5FA)

// ── Neutral palette ────────────────────────────────────────────────────────────
val Neutral50  = Color(0xFFFAFAFA)
val Neutral100 = Color(0xFFF4F4F5)
val Neutral200 = Color(0xFFE4E4E7)
val Neutral300 = Color(0xFFD1D5DB)
val Neutral400 = Color(0xFF9CA3AF)
val Neutral500 = Color(0xFF6B7280)
val Neutral600 = Color(0xFF4B5563)
val Neutral700 = Color(0xFF374151)
val Neutral800 = Color(0xFF1F2937)
val Neutral900 = Color(0xFF111827)
val Neutral950 = Color(0xFF0A0A0A)

// ── Semantic ───────────────────────────────────────────────────────────────────
val ErrorRed = Color(0xFFEF4444)

// ── Legacy reader background aliases (kept for compatibility) ──────────────────
val ReaderBackgroundLight = Color(0xFFFFFFFF)
val ReaderBackgroundDark  = Color(0xFF1A1A1A)
val ReaderBackgroundSepia = Color(0xFFF5E6C8)

// ── Accent colour palette ─────────────────────────────────────────────────────
// Each entry: light primary, dark primary (for the dynamic scheme fallback)

data class AccentPalette(
    val light: Color,
    val lightDark: Color,   // on-primary in dark mode
    val dark: Color,        // primary in dark mode
    val container: Color,   // primaryContainer
    val name: String,
)

val AccentPalettes: Map<AccentColor, AccentPalette> = mapOf(
    AccentColor.BLUE   to AccentPalette(Color(0xFF2563EB), Color(0xFF003380), Color(0xFF60A5FA), Color(0xFFDEEAFF), "Blue"),
    AccentColor.INDIGO to AccentPalette(Color(0xFF4F46E5), Color(0xFF1E1B8B), Color(0xFF818CF8), Color(0xFFE0E7FF), "Indigo"),
    AccentColor.PURPLE to AccentPalette(Color(0xFF7C3AED), Color(0xFF3B0089), Color(0xFFA78BFA), Color(0xFFEDE9FE), "Purple"),
    AccentColor.PINK   to AccentPalette(Color(0xFFDB2777), Color(0xFF7A003F), Color(0xFFF472B6), Color(0xFFFFE4EF), "Pink"),
    AccentColor.RED    to AccentPalette(Color(0xFFDC2626), Color(0xFF7F1D1D), Color(0xFFF87171), Color(0xFFFFE4E4), "Red"),
    AccentColor.ORANGE to AccentPalette(Color(0xFFEA580C), Color(0xFF7C2D12), Color(0xFFFB923C), Color(0xFFFFEDD5), "Orange"),
    AccentColor.AMBER  to AccentPalette(Color(0xFFD97706), Color(0xFF78350F), Color(0xFFFBBF24), Color(0xFFFEF3C7), "Amber"),
    AccentColor.GREEN  to AccentPalette(Color(0xFF16A34A), Color(0xFF064E3B), Color(0xFF4ADE80), Color(0xFFDCFCE7), "Green"),
    AccentColor.TEAL   to AccentPalette(Color(0xFF0D9488), Color(0xFF134E4A), Color(0xFF2DD4BF), Color(0xFFCCFBF1), "Teal"),
    AccentColor.SLATE  to AccentPalette(Color(0xFF475569), Color(0xFF1E293B), Color(0xFF94A3B8), Color(0xFFE2E8F0), "Slate"),
)

// ── Reader background colours ─────────────────────────────────────────────────

data class ReaderBgPalette(
    val background: Color,
    val textHint: Color,      // colour used for lines/placeholder content
    val isDark: Boolean,
    val name: String,
)

val ReaderBackgrounds: Map<ReaderBackground, ReaderBgPalette> = mapOf(
    ReaderBackground.WHITE       to ReaderBgPalette(Color(0xFFFFFFFF), Color(0xFFD1D5DB), false, "White"),
    ReaderBackground.LIGHT_GREY  to ReaderBgPalette(Color(0xFFF5F5F5), Color(0xFFD1D5DB), false, "Light Grey"),
    ReaderBackground.CREAM       to ReaderBgPalette(Color(0xFFFFFBF0), Color(0xFFD6CDB4), false, "Cream"),
    ReaderBackground.SEPIA       to ReaderBgPalette(Color(0xFFF5E6C8), Color(0xFFC4A97A), false, "Sepia"),
    ReaderBackground.WARM_SEPIA  to ReaderBgPalette(Color(0xFFEDD9A3), Color(0xFFB89050), false, "Warm Sepia"),
    ReaderBackground.ROSE        to ReaderBgPalette(Color(0xFFFFF1F2), Color(0xFFFBCAD0), false, "Rose"),
    ReaderBackground.DARK        to ReaderBgPalette(Color(0xFF1A1A1A), Color(0xFF3A3A3A), true,  "Dark"),
    ReaderBackground.DARK_GREY   to ReaderBgPalette(Color(0xFF2D2D2D), Color(0xFF4A4A4A), true,  "Dark Grey"),
    ReaderBackground.DARK_BLUE   to ReaderBgPalette(Color(0xFF0F172A), Color(0xFF1E3A5F), true,  "Dark Blue"),
    ReaderBackground.DARK_GREEN  to ReaderBgPalette(Color(0xFF0F1F0F), Color(0xFF1A3A1A), true,  "Dark Green"),
    ReaderBackground.DARK_BROWN  to ReaderBgPalette(Color(0xFF1C1209), Color(0xFF3A2A10), true,  "Dark Brown"),
    ReaderBackground.AMOLED      to ReaderBgPalette(Color(0xFF000000), Color(0xFF2A2A2A), true,  "AMOLED Black"),
)

/** Resolve the actual background [Color] for a [ReaderBackground] value. */
fun ReaderBackground.toColor(): Color =
    ReaderBackgrounds[this]?.background ?: Color.White
