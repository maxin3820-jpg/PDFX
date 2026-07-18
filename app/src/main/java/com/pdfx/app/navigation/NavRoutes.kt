package com.pdfx.app.navigation

/**
 * Sealed class representing every navigation destination in PDFX.
 * Using sealed class ensures compile-time exhaustiveness checks.
 */
sealed class NavRoutes(val route: String) {

    /** PDF library grid */
    data object Home : NavRoutes("home")

    /**
     * PDF reader — opened from the library.
     * Receives the document's database ID via the route path.
     */
    data object Reader : NavRoutes("reader/{documentId}") {
        const val ARG_DOCUMENT_ID = "documentId"
        fun createRoute(documentId: Long) = "reader/$documentId"
    }

    /**
     * PDF reader — opened from an external intent (temporary).
     * The URI is passed as an encoded query parameter.
     */
    data object ReaderTemp : NavRoutes("reader_temp?uri={uri}") {
        const val ARG_URI = "uri"
        fun createRoute(encodedUri: String) = "reader_temp?uri=$encodedUri"
    }

    /** App settings */
    data object Settings : NavRoutes("settings")
}
