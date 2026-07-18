package com.pdfx.app.data.database

import com.pdfx.app.domain.model.PdfDocument

/**
 * Bidirectional mappers between Room [PdfEntity] and domain [PdfDocument].
 * Keeping this logic here avoids polluting either layer.
 */

fun PdfEntity.toDomain(): PdfDocument = PdfDocument(
    id = id,
    uri = uri,
    displayName = displayName,
    fileName = fileName,
    fileSize = fileSize,
    pageCount = pageCount,
    importedAt = importedAt,
    lastPageIndex = lastPageIndex,
    lastZoom = lastZoom,
    lastScrollOffset = lastScrollOffset,
)

fun PdfDocument.toEntity(): PdfEntity = PdfEntity(
    id = id,
    uri = uri,
    displayName = displayName,
    fileName = fileName,
    fileSize = fileSize,
    pageCount = pageCount,
    importedAt = importedAt,
    lastPageIndex = lastPageIndex,
    lastZoom = lastZoom,
    lastScrollOffset = lastScrollOffset,
)
