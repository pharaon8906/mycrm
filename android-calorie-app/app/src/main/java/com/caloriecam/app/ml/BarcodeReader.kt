package com.caloriecam.app.ml

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** On-device barcode detection (EAN/UPC/QR) from a captured photo — no network involved. */
class BarcodeReader {

    private val scanner = BarcodeScanning.getClient()

    suspend fun read(bitmap: Bitmap): List<Barcode> = withContext(Dispatchers.Default) {
        val image = InputImage.fromBitmap(bitmap, 0)
        Tasks.await(scanner.process(image))
    }
}
