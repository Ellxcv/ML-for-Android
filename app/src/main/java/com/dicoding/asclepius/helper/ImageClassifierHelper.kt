package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.dicoding.asclepius.ml.CancerClassification
import org.tensorflow.lite.support.image.TensorImage

class ImageClassifierHelper(private val context: Context, private val listener: ClassifierListener) {

    // Lazy initialization of the model
    private val model: CancerClassification by lazy {
        CancerClassification.newInstance(context)
    }

    // Function to classify a static image
    fun classifyStaticImage(imageUri: Uri) {
        try {
            val bitmap = getBitmapFromUri(imageUri)
            val tensorImage = TensorImage.fromBitmap(bitmap)

            // Perform classification and get the result
            val outputs = model.process(tensorImage)
            val probability = outputs.probabilityAsCategoryList

            // Find the top prediction by score
            val topPrediction = probability.maxByOrNull { it.score }

            // Return the prediction and score if available
            if (topPrediction != null) {
                listener.onClassificationSuccess(Pair(topPrediction.label, topPrediction.score))
            } else {
                listener.onClassificationError("No classification result found.")
            }
        } catch (e: Exception) {
            // Handle error and pass it to the listener
            listener.onClassificationError("Error during classification: ${e.message}")
        }
    }

    // Helper function to convert URI to bitmap
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }.copy(Bitmap.Config.ARGB_8888, true) // Ensure ARGB_8888 config if needed
    }

    // Close the model when not in use to avoid memory leaks
    fun closeModel() {
        model.close()
    }
}
