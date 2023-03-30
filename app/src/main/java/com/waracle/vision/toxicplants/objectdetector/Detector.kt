package com.waracle.vision.toxicplants.objectdetector

import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject

interface Detector {

    sealed class DetectionResult : Message {
        class SUCCESS(
            val label: String,
            val confidence: Float,
            val bounds: Rect?
        ) : DetectionResult() {
            override fun toString(): String = "$label \n$confidence\nBounds = ${bounds?.toString()}"
        }

        class ERROR(val reason: String) : DetectionResult() {
            override fun toString(): String = reason
        }
    }

    suspend fun processImage(imageProxy: ImageProxy): DetectionResult
}