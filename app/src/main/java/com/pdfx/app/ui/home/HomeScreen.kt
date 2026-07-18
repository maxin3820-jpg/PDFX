package com.pdfx.app.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdfx.app.domain.model.GridLayout
import com.pdfx.app.domain.model.CardStyle
import com.pdfx.app.ui.components.DuplicateDialog
import com.pdfx.app.ui.components.EmptyLibrary
import com.pdfx.app.ui.components.PdfCard
import com.pdfx.app.ui.components.RenameDialog
import com.pdfx.app.viewmodel.HomeViewModel

// No typealias needed — PdfxNavGraph imports HomeScreen directly

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenDocument: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // Show error snackbar
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    // SAF multi-select file picker — PDF only
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importPdfs(uris)
        }
    }

    fun launchPicker() {
        filePicker.launch(arrayOf("application/pdf"))
    }

    // Thumbnail cache is provided by the ViewModel
    val thumbnailCache = viewModel.thumbnailCache

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PDFX",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = documents.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FloatingActionButton(
                    onClick = ::launchPicker,
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Import PDF",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (documents.isEmpty()) {
                EmptyLibrary(
                    onImportClick = ::launchPicker,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                val columns = when (settings.gridLayout) {
                    GridLayout.TWO_COLUMN    -> 2
                    GridLayout.SINGLE_COLUMN -> 1
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(
                        items = documents,
                        key = { it.id },
                        contentType = { "pdf_card" }  // Performance hint for Compose
                    ) { doc ->
                        PdfCard(
                            document = doc,
                            thumbnailCache = thumbnailCache,
                            cardStyle = settings.cardStyle,
                            onClick = { onOpenDocument(doc.id) },
                            onRename = { viewModel.showRenameDialog(doc) },
                            onRemove = { viewModel.removeDocument(doc) },
                        )
                    }
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    uiState.renameTarget?.let { doc ->
        RenameDialog(
            document = doc,
            onConfirm = { newName -> viewModel.renameDocument(doc.id, newName) },
            onDismiss = viewModel::dismissRenameDialog,
        )
    }

    // Bug #6 fix: show head of the queue; when dismissed the next duplicate
    // (if any) automatically surfaces because the list shrinks.
    uiState.pendingDuplicates.firstOrNull()?.let { event ->
        DuplicateDialog(
            documentName = event.existingDocument.displayName,
            onImportAgain = { viewModel.forceImportDuplicate(event.uri) },
            onDismiss = viewModel::dismissDuplicate,
        )
    }
}
