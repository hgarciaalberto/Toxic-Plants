package com.waracle.vision.toxicplants.objectdetector

import android.graphics.Bitmap
import android.graphics.Rect
import com.waracle.vision.toxicplants.MainViewModel

interface Detector {
    suspend fun processImage(bitmap: Bitmap): DetectionResult

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
}