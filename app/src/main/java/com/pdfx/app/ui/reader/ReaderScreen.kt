package com.pdfx.app.ui.reader

import android.graphics.Bitmap
import android.net.Uri
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pdfx.app.domain.model.ReaderTheme
import com.pdfx.app.viewmodel.ReaderUiState
import com.pdfx.app.viewmodel.ReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    documentId: Long?,
    temporaryUri: Uri?,
    onNavigateBack: () -> Unit,
    onAddToLibrary: (() -> Unit)? = null,
    viewModel: ReaderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current

    // Open document on first composition only
    LaunchedEffect(documentId, temporaryUri) {
        when {
            documentId != null && documentId > 0L -> viewModel.openFromLibrary(documentId)
            temporaryUri != null                   -> viewModel.openTemporary(temporaryUri)
        }
    }

    // Keep screen on / off based on settings.
    // Bug #2 fix: also clear the flag immediately when keepScreenOn flips to false.
    // Previously only addFlags was called; clearFlags only ran in onDispose,
    // meaning toggling the setting OFF while reading had no effect until recompose.
    DisposableEffect(uiState.keepScreenOn) {
        val window = (view.context as? android.app.Activity)?.window
        if (uiState.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Handle "add to library" trigger from ViewModel
    LaunchedEffect(uiState.addToLibraryRequested) {
        if (uiState.addToLibraryRequested) {
            viewModel.clearAddToLibraryRequest()
            onAddToLibrary?.invoke()
        }
    }

    val readerBackground = when (uiState.readerTheme) {
        ReaderTheme.LIGHT -> Color.White
        ReaderTheme.DARK  -> Color(0xFF1A1A1A)
        ReaderTheme.SEPIA -> Color(0xFFF5E6C8)
    }
    val topBarContainerColor = when (uiState.readerTheme) {
        ReaderTheme.DARK -> Color(0xF0121212)
        else             -> MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
    }

    Scaffold(
        containerColor = readerBackground,
        topBar = {
            AnimatedVisibility(
                visible = uiState.topBarVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit  = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    actions = {
                        if (uiState.isTemporary) {
                            IconButton(onClick = viewModel::addToLibrary) {
                                Icon(
                                    imageVector = Icons.Outlined.BookmarkAdd,
                                    contentDescription = "Add to Library",
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topBarContainerColor,
                    ),
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) { Text("Go back") }
                    }
                }
            }

            uiState.pageCount > 0 -> {
                PdfPageList(
                    uiState = uiState,
                    viewModel = viewModel,
                    readerBackground = readerBackground,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Scrollable page list with pinch-to-zoom and double-tap zoom
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PdfPageList(
    uiState: ReaderUiState,
    viewModel: ReaderViewModel,
    readerBackground: Color,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = uiState.currentPage.coerceAtLeast(0)
    )

    // Track visible page index from scroll state
    val currentPage by remember { derivedStateOf { listState.firstVisibleItemIndex } }

    // Bug #5 fix: track page changes and scroll offset independently.
    // Previously both were saved inside LaunchedEffect(currentPage), so the
    // offset was only captured at page-boundary crossings, not continuously.
    // Now page index and scroll offset are observed via separate snapshotFlows
    // so the persisted offset is always the user's actual current position.
    LaunchedEffect(currentPage) {
        viewModel.onPageChanged(currentPage)
    }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset ->
                viewModel.onScrollOffsetChanged(offset.toFloat())
            }
    }

    // Bug #4 fix: initialize scale once without a key so it is NOT reset every
    // time uiState.zoom changes. Previously `remember(uiState.zoom)` caused a
    // feedback loop: each pinch wrote scale → uiState.zoom → reset scale → jitter.
    // The initial value still comes from the restored reading position on open.
    var scale by remember { mutableFloatStateOf(uiState.zoom.coerceIn(1f, 5f)) }

    val transformableState = rememberTransformableState { zoomChange, _, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        viewModel.onZoomChanged(scale)
    }

    BoxWithConstraints(modifier = modifier) {
        val containerWidthPx = constraints.maxWidth

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { viewModel.onTap() },
                        onDoubleTap = {
                            // Toggle between fit (1×) and comfortable reading zoom (2.5×)
                            scale = if (scale > 1.2f) 1f else 2.5f
                            viewModel.onZoomChanged(scale)
                        }
                    )
                }
                .transformable(state = transformableState),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
            ) {
                items(uiState.pageCount) { pageIndex ->
                    PdfPageItem(
                        pageIndex = pageIndex,
                        targetWidth = containerWidthPx,
                        viewModel = viewModel,
                        backgroundColor = readerBackground,
                    )
                }
            }

            // Page number indicator overlay
            if (uiState.showPageNumber && uiState.pageCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.50f),
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = "${currentPage + 1} / ${uiState.pageCount}",
                        color = Color.White,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Single page item — rendered on demand via PdfRenderer with aggressive caching
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PdfPageItem(
    pageIndex: Int,
    targetWidth: Int,
    viewModel: ReaderViewModel,
    backgroundColor: Color,
) {
    // Key by BOTH pageIndex and targetWidth to re-render on orientation change
    var bitmap by remember(pageIndex, targetWidth) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(pageIndex, targetWidth) {
        if (targetWidth > 0) {
            bitmap = viewModel.renderPage(pageIndex, targetWidth)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(backgroundColor)
            .padding(bottom = 1.dp),        // minimal separator between pages
        contentAlignment = Alignment.TopCenter,
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Page ${pageIndex + 1}",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            // Loading placeholder — same aspect ratio as A4
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp)
            }
        }
    }
}
