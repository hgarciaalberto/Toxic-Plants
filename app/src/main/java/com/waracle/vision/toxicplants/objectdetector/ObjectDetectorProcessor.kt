package com.waracle.vision.toxicplants.objectdetector

import android.graphics.Rect
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ObjectDetectorProcessor @Inject constructor() : Detector {

    private val objectDetector = createObjectDetector()

    private fun createObjectDetector(): ObjectDetector {

        val localModel = LocalModel.Builder()
            .setAssetFilePath("lite-model_aiy_vision_classifier_plants_V1_3.tflite")
            .build()

        val options =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(3)
                .build()

        return ObjectDetection.getClient(options)
    }

    @androidx.camera.core.ExperimentalGetImage
    override suspend fun processImage(imageProxy: ImageProxy): Detector.DetectionResult =
        suspendCoroutine { continuation ->
            Timber.d("Image resolution: ${imageProxy.width} x ${imageProxy.height}")
            val image: Image? = imageProxy.image
            image?.let { img ->
                val iImage = InputImage
                    .fromMediaImage(img, imageProxy.imageInfo.rotationDegrees)
                objectDetector.process(iImage)
                    .addOnSuccessListener { detectedObjects ->
                        Timber.d("Found objects processImage: ${detectedObjects.size}")
                        detectedObjects.firstOrNull()?.let { detectedObject ->

                            val boundingBox: Rect = detectedObject.boundingBox
                            val clippedBox = Rect(
                                maxOf(0, boundingBox.left),
                                maxOf(0, boundingBox.top),
                                minOf(imageProxy.width, boundingBox.right),
                                minOf(imageProxy.height, boundingBox.bottom)
                            )

                            val trackingId = detectedObject.trackingId
                            for (label in detectedObject.labels) {
                                val text = label.text
                                val index = label.index
                                val confidence = label.confidence
                                Timber.d(
                                    "Detected object with label: $text, confidence: $confidence"
                                )
                                val result = Detector.DetectionResult.SUCCESS_SINGLE(
                                    label = text,
                                    confidence = confidence,
                                    bounds = listOf(clippedBox)
                                )
                                continuation.resume(result)
                            }
                        }
                    }
                    .addOnFailureListener {
                        val result =
                            Detector.DetectionResult.ERROR(it.message ?: it.javaClass.simpleName)
                        continuation.resume(result)
                    }
                    .addOnCompleteListener {
                        image.close()
                        imageProxy.close()
                    }
            }
        }

    companion object {
        val TAG = this::class.java.toString()
    }
}