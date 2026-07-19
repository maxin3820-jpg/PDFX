package com.pdfx.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.pdfx.app.domain.model.AppTheme
import com.pdfx.app.navigation.NavRoutes
import com.pdfx.app.navigation.PdfxNavGraph
import com.pdfx.app.ui.theme.PdfxTheme
import com.pdfx.app.viewmodel.HomeViewModel
import com.pdfx.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity — the entire app UI is Compose.
 *
 * External PDF opens (from WhatsApp, Gmail, file managers, etc.) arrive via
 * ACTION_VIEW intents. PDFX opens them immediately in "temporary" reader mode
 * without adding them to the library unless the user explicitly taps
 * "Add to Library".
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Activity-scoped ViewModels — outlive individual composable back-stack entries
    private val mainViewModel: MainViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val externalUri: Uri? = resolveExternalPdfUri()

        setContent {
            val appTheme by mainViewModel.appTheme.collectAsState(initial = AppTheme.SYSTEM)
            val accentColor by mainViewModel.accentColor.collectAsState(
                initial = com.pdfx.app.domain.model.AccentColor.BLUE
            )

            PdfxTheme(appTheme = appTheme, accentColor = accentColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val navController = rememberNavController()

                    val startDestination = if (externalUri != null) {
                        NavRoutes.ReaderTemp.createRoute(Uri.encode(externalUri.toString()))
                    } else {
                        NavRoutes.Home.route
                    }

                    PdfxNavGraph(
                        navController = navController,
                        startDestination = startDestination,
                        onAddTemporaryToLibrary = { uri ->
                            homeViewModel.importPdfs(listOf(uri))
                        },
                    )
                }
            }
        }
    }

    /**
     * Extracts a PDF URI from an external ACTION_VIEW intent.
     * Returns null if the app was launched from the launcher normally.
     */
    private fun resolveExternalPdfUri(): Uri? {
        if (intent?.action != android.content.Intent.ACTION_VIEW) return null
        val uri = intent?.data ?: return null
        val mimeType = intent.type ?: contentResolver.getType(uri)
        return if (
            mimeType == "application/pdf" ||
            uri.toString().endsWith(".pdf", ignoreCase = true)
        ) uri else null
    }
}
