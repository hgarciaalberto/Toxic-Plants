package com.waracle.vision.cncddetector

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Rect
import android.graphics.RectF
import androidx.camera.core.ImageProxy
import com.waracle.vision.opencvdetector.toBoundingRect
import com.waracle.vision.toxicplants.objectdetector.Detector
import com.waracle.vision.toxicplants.rotate
import com.waracle.vision.toxicplants.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class MobileCCDNDetector @Inject constructor(
    @ApplicationContext private val context: Context
): Detector {

    private var cncdDetectorInner: MobilenetSSDNcnn = MobilenetSSDNcnn()
    var isInitialised: Boolean = false
    private set

    fun init() {
        cncdDetectorInner.Init(context.assets)
        isInitialised = true
    }

    override suspend fun processImageMultipleResults(imageProxy: ImageProxy): List<Detector.DetectionResult> {
        val inputBitmap = imageProxy.toBitmap()
        try {
            val detectionResults = inputBitmap?.let { bitmap ->
                cncdDetectorInner.Detect(bitmap, true)
            }?.mapNotNull { detection ->
                Detector.DetectionResult.ResultItem(
                    label = detection?.label ?: "unknown",
                    confidence = detection?.prob ?: 0f,
                    bound = detection?.toBoundingRect() ?: Rect()
                )
            } ?: listOf(Detector.DetectionResult.ERROR("None"))
            return if (detectionResults.isNotEmpty()) detectionResults
            else listOf(Detector.DetectionResult.ERROR("None"))
        } catch (e: Exception) {
            return listOf(Detector.DetectionResult.ERROR("No detection results from cncd"))
        } finally {
            imageProxy.close()
        }
        return listOf(Detector.DetectionResult.ERROR("Something went wrong"))
    }


}