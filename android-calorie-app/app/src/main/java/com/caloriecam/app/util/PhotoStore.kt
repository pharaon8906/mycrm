package com.caloriecam.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Keeps captured/imported food photos in app-private storage
 * (files/photos/) so entries survive across gallery/camera Uri
 * permission churn.
 */
object PhotoStore {

    private fun photosDir(context: Context): File {
        val dir = File(context.filesDir, "photos")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /** New empty file for the system camera to write into. */
    fun newCameraFile(context: Context, prefix: String = "IMG"): File =
        File(photosDir(context), "${prefix}_${System.currentTimeMillis()}.jpg")

    /** Content Uri usable by other apps (e.g. the camera Intent). */
    fun contentUriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    /** Copies a picked gallery image into app-private storage. */
    fun copyToInternal(context: Context, source: Uri, prefix: String = "IMG"): File {
        val dest = File(photosDir(context), "${prefix}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(source)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        return dest
    }

    fun delete(file: File) {
        runCatching { file.delete() }
    }
}
