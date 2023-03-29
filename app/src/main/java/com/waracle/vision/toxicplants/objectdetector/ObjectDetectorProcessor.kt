package com.waracle.vision.toxicplants.objectdetector

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class ObjectDetectorProcessor @Inject constructor(): Detector{

    private val objectDetector = createObjectDetector()

    private fun createObjectDetector(): ObjectDetector {
        // [START create_local_model]
        val localModel = LocalModel.Builder()
            .setAssetFilePath("lite-model_aiy_vision_classifier_plants_V1_3.tflite") // or .setAbsoluteFilePath("absolute_file_path_to_tflite_model")
            .build()
        // [END create_local_model]

        // [START create_custom_options]
        // Live detection and tracking
        val options = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
            .enableClassification()
            .setClassificationConfidenceThreshold(0.5f)
            .setMaxPerObjectLabelCount(3)
            .build()

        // Multiple object detection in static images
//        val options = CustomObjectDetectorOptions.Builder(localModel)
//            .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
//            .enableMultipleObjects()
//            .enableClassification()
//            .setClassificationConfidenceThreshold(0.5f)
//            .setMaxPerObjectLabelCount(1)
//            .build()
        // [END create_custom_options]

        return ObjectDetection.getClient(options)
    }

    override suspend fun processImage(bitmap: Bitmap): Detector.DetectionResult = suspendCoroutine { continuation ->
        Log.d(TAG, "Requested processImage")
        val image = InputImage.fromBitmap(
            bitmap,
            90
        )
        Log.d(TAG, "Start processImage")
        // [START process_image]
        objectDetector.process(image)
            .addOnSuccessListener { detectedObjects -> // Task completed successfully
                // ...
                Log.d(TAG, "Found objects processImage: ${detectedObjects.size}")
                detectedObjects.firstOrNull()?.let { detectedObject ->
                    val boundingBox: Rect = detectedObject.boundingBox
                    val trackingId = detectedObject.trackingId
                    for (label in detectedObject.labels) {
                        val text = label.text
                        val index = label.index
                        val confidence = label.confidence
                        Log.d(TAG, "Detected object with label: $text, confidence: $confidence")
                        val result = Detector.DetectionResult.SUCCESS(
                            label = text,
                            confidence = confidence,
                            bounds = boundingBox
                        )
                        continuation.resume(result)
                    }
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Error: ${it.message}")
                continuation.resume(Detector.DetectionResult.ERROR(it.message ?: "Error"))
            }
    }

    companion object {
        val TAG = this::class.java.toString()
    }
}