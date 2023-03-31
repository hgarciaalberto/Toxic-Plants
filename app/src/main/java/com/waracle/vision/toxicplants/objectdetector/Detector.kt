package com.waracle.vision.toxicplants.objectdetector

import android.graphics.Rect
import androidx.camera.core.ImageProxy

interface Detector {

    sealed class DetectionResult : Message {
        class SUCCESS(
            val label: String,
            val confidence: Float,
            val bounds: Rect?
        ) : DetectionResult() {
            override fun toString(): String = """
                $label 
                $confidence
                ${bounds?.toString()}
            """.trimIndent()
        }

        class ERROR(val reason: String) : DetectionResult() {
            override fun toString(): String = reason
        }
    }

    suspend fun processImage(imageProxy: ImageProxy): DetectionResult
}