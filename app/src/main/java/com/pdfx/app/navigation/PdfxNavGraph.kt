package com.pdfx.app.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pdfx.app.ui.home.HomeScreen
import com.pdfx.app.ui.reader.ReaderScreen
import com.pdfx.app.ui.settings.SettingsScreen

private const val ANIM_DURATION = 300

@Composable
fun PdfxNavGraph(
    navController: NavHostController,
    startDestination: String = NavRoutes.Home.route,
    /**
     * Called when the user taps "Add to Library" while viewing a temporary PDF.
     * The URI is then handled by [HomeScreen]'s ViewModel via [HomeScreen].
     * Passed down from MainActivity so it has a stable reference to HomeViewModel.
     */
    onAddTemporaryToLibrary: (Uri) -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(ANIM_DURATION)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                tween(ANIM_DURATION)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(ANIM_DURATION)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                tween(ANIM_DURATION)
            )
        },
    ) {
        // ── Home ──────────────────────────────────────────────────────────────
        composable(route = NavRoutes.Home.route) {
            HomeScreen(
                onOpenDocument = { documentId ->
                    navController.navigate(NavRoutes.Reader.createRoute(documentId))
                },
                onOpenSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                }
            )
        }

        // ── Reader — opened from library ──────────────────────────────────────
        composable(
            route = NavRoutes.Reader.route,
            arguments = listOf(
                navArgument(NavRoutes.Reader.ARG_DOCUMENT_ID) {
                    type = NavType.LongType
                }
            ),
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments
                ?.getLong(NavRoutes.Reader.ARG_DOCUMENT_ID) ?: -1L
            ReaderScreen(
                documentId = documentId,
                temporaryUri = null,
                onNavigateBack = { navController.popBackStack() },
            )
        }

        // ── Reader — temporary open from external intent ───────────────────────
        composable(
            route = NavRoutes.ReaderTemp.route,
            arguments = listOf(
                navArgument(NavRoutes.ReaderTemp.ARG_URI) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = { fadeIn(tween(ANIM_DURATION)) },
            exitTransition  = { fadeOut(tween(ANIM_DURATION)) },
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments
                ?.getString(NavRoutes.ReaderTemp.ARG_URI)
            val uri = encodedUri?.let { Uri.parse(Uri.decode(it)) }

            ReaderScreen(
                documentId = null,
                temporaryUri = uri,
                onNavigateBack = { navController.popBackStack() },
                onAddToLibrary = {
                    // Bug #9 fix: navigation was unconditional — if uri was null
                    // the import was silently skipped but the user was still sent
                    // to the Home screen with no feedback and no new document.
                    // Now we only navigate when the import can actually succeed.
                    // Also changed inclusive = false so the existing Home instance
                    // is reused rather than destroyed and recreated.
                    if (uri != null) {
                        onAddTemporaryToLibrary(uri)
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Home.route) { inclusive = false }
                        }
                    }
                },
            )
        }

        // ── Settings ──────────────────────────────────────────────────────────
        composable(
            route = NavRoutes.Settings.route,
            enterTransition = { fadeIn(tween(ANIM_DURATION)) },
            exitTransition  = { fadeOut(tween(ANIM_DURATION)) },
        ) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
