package com.waracle.vision.toxicplants

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.modeldownloader.CustomModel
import org.tensorflow.lite.DataType.FLOAT32
import org.tensorflow.lite.DataType.UINT8
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PlantDetector(private val model: Any) {

    private lateinit var interpreter: Interpreter

    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0
    private var inputChannels: Int = 0
    private var modelInputSize: Int = 0

    init {
        // Initialize TF Lite Interpreter with NNAPI enabled
        val options = Interpreter.Options().apply {
            setUseNNAPI(true)
        }
        when (model) {
            is ByteBuffer -> {
                interpreter = Interpreter(model, options)
            }
            is CustomModel -> {
                model.file?.let { modelFile ->
                    interpreter = Interpreter(modelFile, options)
                    // Read input shape from model file
                    val inputShape = interpreter.getInputTensor(0).shape()
                    inputImageWidth = inputShape[1]
                    inputImageHeight = inputShape[2]
                    inputChannels = inputShape[3]

                    val inputDataType = when (interpreter.getInputTensor(0).dataType()) {
                        FLOAT32 -> FLOAT32.byteSize()
                        UINT8 -> UINT8.byteSize()
                        else -> throw IllegalArgumentException("Unsupported data type for input tensor.")
                    }
                    modelInputSize = inputDataType * inputImageWidth * inputImageHeight * inputChannels

                    // Finish interpreter initialization
                    Log.d(TAG, "Initialized TFLite interpreter.")
                }
            }
            else -> {
                Log.e(TAG, "Unexpected model type downloaded.")
            }
        }
    }


    fun processImage(bitmap: Bitmap): String {
        if (!this::interpreter.isInitialized) {
            throw IllegalStateException("TF Lite Interpreter is not initialized yet.")
        }

        // Preprocessing: resize the input
        var startTime = System.nanoTime()
        val resizedImage = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)


        val byteBuffer = resizedImage.convertBitmapToByteBuffer()

        var elapsedTime = (System.nanoTime() - startTime) / 1000000
        Log.d(TAG, "Preprocessing time = " + elapsedTime + "ms")

        var startTime2 = System.nanoTime()
        val result = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }
        interpreter.run(byteBuffer, result)
        var elapsedTime2 = (System.nanoTime() - startTime2) / 1000000
        Log.d(TAG, "Inference time = " + elapsedTime2 + "ms")

        return getOutputString(result[0])
    }

    private fun Bitmap.convertBitmapToByteBuffer(): ByteBuffer {

        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())


//        val bmp32 = Bitmap.createBitmap(inputImageWidth, inputImageHeight, Bitmap.Config.ARGB_8888)
//        var buffer = ByteBuffer.allocate(bmp32.byteCount)
//        bmp32.copyPixelsToBuffer(buffer)
//        val bytes = buffer.array()

        val bmp8 = Bitmap.createBitmap(inputImageWidth, inputImageHeight, Bitmap.Config.RGB_565)
        val buffer2 = ByteBuffer.wrap(byteBuffer.array())
        bmp8.copyPixelsFromBuffer(buffer2)


        val pixels = IntArray(inputImageWidth * inputImageHeight)
        bmp8.getPixels(pixels, 0, width, 0, 0, width, height)

        try {

            for (pixelValue in pixels) {
                val r = (pixelValue shr 32 and 0xFF)
                val g = (pixelValue shr 16 and 0xFF)
                val b = (pixelValue and 0xFF)

                // Convert RGB to grayscale and normalize pixel value to [0..1]
//                val normalizedPixelValue1 = (r + g + b) / 3.0f / 255.0f
//            val normalizedPixelValue1 = (r + g + b).toFloat()
//            byteBuffer.putFloat(normalizedPixelValue1)


                // Convert RGB to grayscale and normalize pixel value to [0..1]
                // Convert pixel value to the [-1, 1] range using the formula:
                //    (pixelValue - mean) / std
                (r - IMAGE_MEAN) / IMAGE_STD.apply {
                    byteBuffer.putFloat(this.toFloat())
                }
                (g - IMAGE_MEAN) / IMAGE_STD.apply {
                    byteBuffer.putFloat(this.toFloat())
                }
                (b - IMAGE_MEAN) / IMAGE_STD.apply {
                    byteBuffer.putFloat(this.toFloat())
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error: ${ex.message}")
        }

        return byteBuffer
    }

//    private fun Bitmap.convertBitmapToByteBuffer(): ByteBuffer {
//        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
//        byteBuffer.order(ByteOrder.nativeOrder())
//
//        val pixels = IntArray(inputImageWidth * inputImageHeight)
//        getPixels(pixels, 0, width, 0, 0, width, height)
//
//        for (pixelValue in pixels) {
//            val r = (pixelValue shr 16 and 0xFF)
//            val g = (pixelValue shr 8 and 0xFF)
//            val b = (pixelValue and 0xFF)
//
//            // Convert RGB to grayscale and normalize pixel value to [0..1]
////            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
//            val normalizedPixelValue = (r + g + b).toFloat()
//            byteBuffer.putFloat(normalizedPixelValue)
//
//        }
//
//        return byteBuffer
//    }

    private fun getOutputString(output: FloatArray): String {
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1
        return """
          Prediction Result: %d
          Confidence: %2f"
        """.trimIndent().format(maxIndex, output[maxIndex])
    }

    companion object {
        private var TAG = PlantDetector::class.java.name
        private var IMAGE_MEAN = 127.5
        private var IMAGE_STD = 127.5


        private const val OUTPUT_CLASSES_COUNT = 10
    }
}