package com.waracle.vision.opencvdetector

import android.content.Context
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.waracle.vision.toxicplants.objectdetector.Detector
import dagger.hilt.android.qualifiers.ApplicationContext
import org.opencv.core.*
import org.opencv.dnn.Dnn
import org.opencv.dnn.Net
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import javax.inject.Inject

class OpenCvDetector @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val openCVLoader: OpenCVLoader
) : Detector {

    private var mNet: Net? = null

    var isInitialised: Boolean = false
        private set

    companion object {
        private const val TAG = "OpenCvDetector"
        private const val CONFIDENCE_THRESHOLD = 0.1f
    }

    init {
        initialize()
    }

    private fun initialize() {
        openCVLoader.loadOpenCV { isLoaded ->
            mNet = loadMobileNetSSDModel(appContext)
            isInitialised = true
        }
    }

    private fun loadMobileNetSSDModel(context: Context): Net? {
        val protoFileName = "deploy.prototxt" // Replace with the correct file name
        val weightsFileName =
            "mobilenet_iter_73000.caffemodel" // Replace with the correct file name

        try {
            val protoFile = getAssetFile(context, protoFileName)
            val weightsFile = getAssetFile(context, weightsFileName)

            if (protoFile != null && weightsFile != null) {
                return Dnn.readNetFromCaffe(protoFile.absolutePath, weightsFile.absolutePath)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    @Throws(IOException::class)
    private fun getAssetFile(context: Context, assetName: String): File? {
        try {
            val file = File(context.filesDir, assetName)
            if (file.exists() && file.length() > 0) {
                return file
            }

            val assetManager = context.assets
            assetManager.open(assetName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return file
        } catch (e: Exception) {
            return null
        }
    }

    override suspend fun processImage(imageProxy: ImageProxy): Detector.DetectionResult {
        imageProxy.use {
            mNet?.let { net ->
                val yuvMat = convertYUVtoMat(imageProxy)

                val inputBlob = Dnn.blobFromImage(
                    yuvMat,
                    0.007843,
                    Size(300.0, 300.0),
                    Scalar(127.5, 127.5, 127.5),
                    false
                )

                net.setInput(inputBlob)

                val detectionMat = net.forward()

                val cols = detectionMat.cols()
                val rows = detectionMat.rows()

                for (i in 0 until rows) {
                    val confidenceData = DoubleArray(1)
                    detectionMat.get(0, i * 7 + 2, confidenceData)
                    val confidence = confidenceData[0]

                    if (confidence > CONFIDENCE_THRESHOLD) {
                        val labelData = DoubleArray(1)
                        detectionMat.get(0, i * 7 + 1, labelData)
                        val labelId = labelData[0].toInt()

                        val label = getLabelName(labelId)

                        val positionData = DoubleArray(4)
                        detectionMat.get(0, i * 7 + 3, positionData)
                        val left = positionData[0] * cols
                        val top = positionData[1] * rows
                        val right = positionData[2] * cols
                        val bottom = positionData[3] * rows

                        val bounds = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

                        return Detector.DetectionResult.SUCCESS_SINGLE(
                            label,
                            confidence.toFloat(),
                            listOf(bounds)
                        )
                    }
                }
                return Detector.DetectionResult.ERROR("No object detected")
            }
            return Detector.DetectionResult.ERROR("MobileNet-SSD model not loaded")
        }
    }

    private fun convertYUVtoMat(img: ImageProxy): Mat {
        val nv21: ByteArray
        val yBuffer: ByteBuffer = img.planes[0].buffer
        val uBuffer: ByteBuffer = img.planes[1].buffer
        val vBuffer: ByteBuffer = img.planes[2].buffer
        val ySize: Int = yBuffer.remaining()
        val uSize: Int = uBuffer.remaining()
        val vSize: Int = vBuffer.remaining()
        nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        val yuv = Mat(img.height + img.height / 2, img.width, CvType.CV_8UC1)
        yuv.put(0, 0, nv21)
        val rgb = Mat()
        Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21, 3)
        Core.rotate(rgb, rgb, Core.ROTATE_90_CLOCKWISE)
        return rgb
    }

    private fun getLabelName(labelId: Int): String {
        // Replace this with your own label mapping
        return "Label $labelId"
    }
}
