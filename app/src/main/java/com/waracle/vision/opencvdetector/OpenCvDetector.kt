package com.waracle.vision.opencvdetector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import androidx.camera.core.ImageProxy
import com.waracle.vision.toxicplants.objectdetector.Detector
import com.waracle.vision.toxicplants.rotate
import com.waracle.vision.toxicplants.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.opencv.android.Utils
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

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override suspend fun processImage(imageProxy: ImageProxy): Detector.DetectionResult {
        imageProxy.use {
            mNet?.let { net ->
                val bitmap = imageProxy.toBitmap()
                val mat = Mat()
                Utils.bitmapToMat(bitmap, mat)
                //val yuvMat = convertYUVtoMat(imageProxy)
                //val mat = convertYUVtoMat(imageProxy)
                //val mat = imageProxy.image?.yuvToRgba()

                val rgb = Mat()
                Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_RGBA2RGB, 3)

                val testBitmap = rgb.toBitmap()
                val inputBlob = Dnn.blobFromImage(
                    rgb,
                    0.007843,
                    Size(imageProxy.width.toDouble(), imageProxy.height.toDouble()),
                    Scalar(127.5, 127.5, 127.5),
                    false
                )

                net.setInput(inputBlob)

                val detectionMat = net.forward()

                //val t2 = detectionMat.toBitmap()

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
        val test1 = yuv.toBitmap()
        val rgb = Mat()
        Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21, 3)
        val test2 = rgb.toBitmap()
        //Core.rotate(rgb, rgb, Core.ROTATE_90_CLOCKWISE)
        return rgb
    }

    fun Image.yuvToRgba(): Mat {
        val rgbaMat = Mat()

        if (format == ImageFormat.YUV_420_888
            && planes.size == 3) {

            val chromaPixelStride = planes[1].pixelStride

            if (chromaPixelStride == 2) { // Chroma channels are interleaved
                assert(planes[0].pixelStride == 1)
                assert(planes[2].pixelStride == 2)
                val yPlane = planes[0].buffer
                val uvPlane1 = planes[1].buffer
                val uvPlane2 = planes[2].buffer
                val yMat = Mat(height, width, CvType.CV_8UC1, yPlane)
                val uvMat1 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane1)
                val uvMat2 = Mat(height / 2, width / 2, CvType.CV_8UC2, uvPlane2)
                val addrDiff = uvMat2.dataAddr() - uvMat1.dataAddr()
                if (addrDiff > 0) {
                    assert(addrDiff == 1L)
                    Imgproc.cvtColorTwoPlane(yMat, uvMat1, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV12)
                } else {
                    assert(addrDiff == -1L)
                    Imgproc.cvtColorTwoPlane(yMat, uvMat2, rgbaMat, Imgproc.COLOR_YUV2RGBA_NV21)
                }
            } else { // Chroma channels are not interleaved
                val yuvBytes = ByteArray(width * (height + height / 2))
                val yPlane = planes[0].buffer
                val uPlane = planes[1].buffer
                val vPlane = planes[2].buffer

                yPlane.get(yuvBytes, 0, width * height)

                val chromaRowStride = planes[1].rowStride
                val chromaRowPadding = chromaRowStride - width / 2

                var offset = width * height
                if (chromaRowPadding == 0) {
                    // When the row stride of the chroma channels equals their width, we can copy
                    // the entire channels in one go
                    uPlane.get(yuvBytes, offset, width * height / 4)
                    offset += width * height / 4
                    vPlane.get(yuvBytes, offset, width * height / 4)
                } else {
                    // When not equal, we need to copy the channels row by row
                    for (i in 0 until height / 2) {
                        uPlane.get(yuvBytes, offset, width / 2)
                        offset += width / 2
                        if (i < height / 2 - 1) {
                            uPlane.position(uPlane.position() + chromaRowPadding)
                        }
                    }
                    for (i in 0 until height / 2) {
                        vPlane.get(yuvBytes, offset, width / 2)
                        offset += width / 2
                        if (i < height / 2 - 1) {
                            vPlane.position(vPlane.position() + chromaRowPadding)
                        }
                    }
                }

                val yuvMat = Mat(height + height / 2, width, CvType.CV_8UC1)
                yuvMat.put(0, 0, yuvBytes)
                Imgproc.cvtColor(yuvMat, rgbaMat, Imgproc.COLOR_YUV2RGBA_I420, 4)
            }
        }

        return rgbaMat
    }

    private fun getLabelName(labelId: Int): String {
        // Replace this with your own label mapping
        return "Label $labelId"
    }

    fun Mat.toBitmap(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
        val bitmap = Bitmap.createBitmap(this.cols(), this.rows(), config)
        Utils.matToBitmap(this, bitmap)
        return bitmap
    }
}
