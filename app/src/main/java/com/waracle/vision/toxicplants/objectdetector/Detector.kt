package com.waracle.vision.toxicplants.objectdetector

import android.graphics.Rect
import androidx.camera.core.ImageProxy

interface Detector {

    sealed class DetectionResult : Message {
        class SUCCESS_SINGLE(
            val label: String,
            val confidence: Float,
            val bounds: List<Rect> = listOf()
        ) : DetectionResult() {
            override fun toString(): String = """
                $label 
                $confidence
            """.trimIndent()
        }

        class ERROR(val reason: String) : DetectionResult() {
            override fun toString(): String = reason
        }

        class ResultItem(
            val label: String,
            val confidence: Float,
            val bound: Rect
        ): DetectionResult() {
            override fun toString(): String = """
                $label 
                $confidence
            """.trimIndent()
        }
    }


    suspend fun processImageMultipleResults(imageProxy: ImageProxy): List<DetectionResult> {
        return listOf()
    }

    suspend fun processImage(imageProxy: ImageProxy): DetectionResult {
        return DetectionResult.ERROR("Not implemented")
    }
}