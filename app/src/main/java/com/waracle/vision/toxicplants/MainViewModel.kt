package com.waracle.vision.toxicplants

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import org.tensorflow.lite.Interpreter

class MainViewModel : ViewModel() {

    val message = MutableStateFlow("Waiting")

    init {
        val conditions = CustomModelDownloadConditions.Builder().requireWifi().build()

        FirebaseModelDownloader.getInstance()
            .getModel("Toxic-Plants-Detector", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
            .addOnCompleteListener {
                // Download complete. Depending on your app, you could enable the ML
                // feature, or switch from the local model to the remote model, etc.

                Log.d(TAG, "status: ${it.isComplete}")
                Log.d(TAG, "status: ${it.isSuccessful}")
                Log.d(TAG, "status: ${it.isCanceled}")

                if (it.isComplete) message.value = "Model isComplete"
                else if (it.isSuccessful) message.value = "Model isSuccessful"
                else if (it.isCanceled) message.value = "Model isCanceled"
                else message.value = "Illegal state"

            }.addOnSuccessListener { model: CustomModel? ->
                Log.d(TAG, "addOnSuccessListener")

                // Download complete. Depending on your app, you could enable the ML
                // feature, or switch from the local model to the remote model, etc.

                // The CustomModel object contains the local path of the model file,
                // which you can use to instantiate a TensorFlow Lite interpreter.
                val modelFile = model?.file
                if (modelFile != null) {
                    message.value = "Interpreter ready"
                    val interpreter = Interpreter(modelFile)
                    Log.d(TAG, "interpreter: ${interpreter.inputTensorCount}")
                }
            }
    }


    companion object {
        private var TAG = MainViewModel::class.java.name
    }
}
