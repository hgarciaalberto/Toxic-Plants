package com.waracle.vision.toxicplants

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer


class PlantDetector {

    private lateinit var interpreter: Interpreter

    private var inputImageWidth: Int = 0
    private var inputImageHeight: Int = 0

    lateinit var probabilityBuffer: TensorBuffer

    val message = MutableStateFlow("Waiting")

    init {
        val conditions = CustomModelDownloadConditions.Builder().build()

        FirebaseModelDownloader.getInstance()
            .getModel("Toxic-Plants-Detector", DownloadType.LOCAL_MODEL, conditions)
            .addOnCompleteListener {
                val model = it.result
                if (model == null) {
                    message.value = "Failed to get model file."
                } else {
                    message.value = "Downloaded remote model: ${model.name}"
                    initInterpreter(model)
                }
            }
            .addOnFailureListener {
                message.value = "Failure: ${it.message}"
            }
    }

    private fun initInterpreter(model: Any) {

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
            return "TF Lite Interpreter is not initialized yet."
        } else {

            val inputShape = interpreter.getInputTensor(0).shape()
            inputImageWidth = inputShape[1]
            inputImageHeight = inputShape[2]


            // Preprocessing: resize the input
            val startTime = System.nanoTime()

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

            val elapsedTime = (System.nanoTime() - startTime) / 1000000
            Log.d(TAG, "Inference time = " + elapsedTime + "ms")

            return getOutputString(probabilityBuffer.floatArray)
        }
    }

    private fun getOutputString(output: FloatArray): String {
        val maxIndex = output.indices.maxByOrNull { output[it] } ?: -1
        return """
          Prediction Result: %d
          Confidence: %.2f%%
        """.trimIndent().format(maxIndex, output[maxIndex] / 255 * 100)
    }

    companion object {
        private var TAG = PlantDetector::class.java.name
    }
}