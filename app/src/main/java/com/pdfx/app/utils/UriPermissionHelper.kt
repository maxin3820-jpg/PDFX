package com.pdfx.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

private const val TAG = "UriPermissionHelper"

/**
 * Helpers for Android Storage Access Framework persistent URI permissions.
 *
 * SAF URIs must be explicitly persisted so the app can reopen files across
 * reboots without prompting the user again.
 */
object UriPermissionHelper {

    /**
     * Persists read permission for [uri].
     * Call this immediately after the user selects a file via the picker.
     */
    fun persist(context: Context, uri: Uri) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Could not persist URI permission for $uri", e)
        }
    }

    /**
     * Checks whether [uri] still has a valid persistent read permission.
     * Returns false if the file was moved or the permission was revoked.
     */
    fun isAccessible(context: Context, uri: Uri): Boolean {
        val persistedUris = context.contentResolver.persistedUriPermissions
        return persistedUris.any { it.uri == uri && it.isReadPermission }
    }

    /**
     * Releases a previously persisted permission for [uri].
     * Call this when the user removes a document from the library.
     */
    fun release(context: Context, uri: Uri) {
        try {
            context.contentResolver.releasePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Could not release URI permission for $uri", e)
        }
    }

    /**
     * Returns the set of URI strings that still have valid persistent
     * read permissions. Used for pruning stale library entries.
     */
    fun getAllAccessibleUris(context: Context): Set<String> =
        context.contentResolver.persistedUriPermissions
            .filter { it.isReadPermission }
            .map { it.uri.toString() }
            .toSet()
}
