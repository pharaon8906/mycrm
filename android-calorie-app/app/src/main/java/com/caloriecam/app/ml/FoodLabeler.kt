package com.caloriecam.app.ml

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-device food/object recognition via ML Kit Image Labeling (bundled model,
 * works fully offline, no API key). Confidence threshold is kept low because
 * the generic label set is not food-specific — results are only "best
 * guesses" used to pre-fill the food picker; the user always confirms.
 */
class FoodLabeler {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.25f)
            .build()
    )

    suspend fun label(bitmap: Bitmap): List<ImageLabel> = withContext(Dispatchers.Default) {
        val image = InputImage.fromBitmap(bitmap, 0)
        Tasks.await(labeler.process(image))
    }
}
