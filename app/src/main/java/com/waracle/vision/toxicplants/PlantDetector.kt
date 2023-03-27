package com.waracle.vision.toxicplants

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.modeldownloader.CustomModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer


class PlantDetector(private val model: Any) {

    private lateinit var interpreter: Interpreter

    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0

    lateinit var probabilityBuffer: TensorBuffer

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

                    interpreter.getInputTensor(0).let { inputTensor ->
                        inputImageWidth = inputTensor.shape()[1]
                        inputImageHeight = inputTensor.shape()[2]
                    }

                    interpreter.getOutputTensor(0).let { outputTensor ->
                        probabilityBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType())
                    }
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

        val inputShape = interpreter.getInputTensor(0).shape()
        inputImageWidth = inputShape[1]
        inputImageHeight = inputShape[2]


        // Preprocessing: resize the input
        var startTime = System.nanoTime()

        // Initialization code
        // Create an ImageProcessor with all ops required. For more ops, please
        // refer to the ImageProcessor Architecture section in this README.
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputImageWidth, inputImageHeight, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        // Create a TensorImage object. This creates the tensor of the corresponding
        // tensor type (uint8 in this case) that the TensorFlow Lite interpreter needs.
        TensorImage(DataType.UINT8).run {
            // Preprocess the image
            load(bitmap)
            imageProcessor.process(this)
        }.let { tensorImage ->
            tensorImage.buffer.rewind()
            probabilityBuffer.buffer.rewind()

            interpreter.run(tensorImage.buffer, probabilityBuffer.buffer)
        }

        var elapsedTime = (System.nanoTime() - startTime) / 1000000
        Log.d(TAG, "Inference time = " + elapsedTime + "ms")

        return getOutputString(probabilityBuffer.floatArray)
    }

    private fun getOutputString(output: FloatArray): String {
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1
        return """
          Prediction Result: %d
          Confidence: %2f"
        """.trimIndent().format(maxIndex, output[maxIndex])
    }

    companion object {
        private var TAG = PlantDetector::class.java.name
    }
}