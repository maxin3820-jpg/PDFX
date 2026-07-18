package com.pdfx.app.domain.model

/**
 * Application-wide settings stored in DataStore.
 */
data class AppSettings(
    val appTheme: AppTheme = AppTheme.SYSTEM,
    val accentColor: AccentColor = AccentColor.BLUE,
    val readerTheme: ReaderTheme = ReaderTheme.LIGHT,
    val readerBackground: ReaderBackground = ReaderBackground.WHITE,
    val cardStyle: CardStyle = CardStyle.ELEVATED,
    val rememberReadingPosition: Boolean = true,
    val defaultZoom: DefaultZoom = DefaultZoom.FIT_WIDTH,
    val gridLayout: GridLayout = GridLayout.TWO_COLUMN,
    val sortOrder: SortOrder = SortOrder.NEWEST,
    val keepScreenOn: Boolean = false,
    val enableAnimations: Boolean = true,
    val showPageNumber: Boolean = true,
)

enum class AppTheme(val value: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
}

/**
 * Accent / primary colour for the app shell (top bar, FAB, toggles, buttons).
 * 10 options covering the most-requested palette choices.
 */
enum class AccentColor(val value: String) {
    BLUE("blue"),           // Default — #2563EB
    INDIGO("indigo"),       // #4F46E5
    PURPLE("purple"),       // #7C3AED
    PINK("pink"),           // #DB2777
    RED("red"),             // #DC2626
    ORANGE("orange"),       // #EA580C
    AMBER("amber"),         // #D97706
    GREEN("green"),         // #16A34A
    TEAL("teal"),           // #0D9488
    SLATE("slate"),         // #475569
}

/**
 * Reader page background — separate from the app shell theme.
 * 12 options covering light, dark, warm, and coloured backgrounds.
 */
enum class ReaderBackground(val value: String) {
    WHITE("white"),                 // Classic white
    LIGHT_GREY("light_grey"),       // #F5F5F5 — softer on eyes
    CREAM("cream"),                 // #FFFBF0 — very light warm
    SEPIA("sepia"),                 // #F5E6C8 — classic sepia
    WARM_SEPIA("warm_sepia"),       // #EDD9A3 — deeper sepia
    ROSE("rose"),                   // #FFF1F2 — subtle rose tint
    DARK("dark"),                   // #1A1A1A — true dark
    DARK_GREY("dark_grey"),         // #2D2D2D — softer dark
    DARK_BLUE("dark_blue"),         // #0F172A — slate dark
    DARK_GREEN("dark_green"),       // #0F1F0F — forest dark
    DARK_BROWN("dark_brown"),       // #1C1209 — warm dark
    AMOLED("amoled"),               // #000000 — pure black AMOLED
}

/**
 * Reader background theme (derived from ReaderBackground — kept for compatibility).
 */
enum class ReaderTheme(val value: String) {
    LIGHT("light"),
    DARK("dark"),
    SEPIA("sepia"),
}

enum class DefaultZoom(val value: String) {
    FIT_WIDTH("fit_width"),
    FIT_HEIGHT("fit_height"),
    HUNDRED_PERCENT("100_percent"),
}

enum class GridLayout(val value: String) {
    TWO_COLUMN("two_column"),
    SINGLE_COLUMN("single_column"),
}

enum class SortOrder(val value: String) {
    NEWEST("newest"),
    OLDEST("oldest"),
    A_TO_Z("a_to_z"),
    Z_TO_A("z_to_a"),
    FILE_SIZE("file_size"),
}

/**
 * Visual style for PDF library cards.
 * 5 options from flat minimal to rich glassmorphism.
 */
enum class CardStyle(val value: String) {
    ELEVATED("elevated"),       // Subtle shadow (default)
    FLAT("flat"),               // No shadow, outline border
    FILLED("filled"),           // Surface-variant fill, no border
    OUTLINED("outlined"),       // Clean border, white background
    COMPACT("compact"),         // Single-line row style, no thumbnail ratio lock
}
