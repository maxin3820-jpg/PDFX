package com.pdfx.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * PDFX Application class.
 * The [HiltAndroidApp] annotation triggers Hilt's code generation for
 * the application-level dependency container.
 */
@HiltAndroidApp
class PdfxApplication : Application()
