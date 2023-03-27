package com.waracle.vision.toxicplants

import android.app.Application
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var plantDetector: PlantDetector

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
                    plantDetector = PlantDetector(model)

//                    application.resources.openRawResource(R.raw.plant).let { stream ->
//                        BitmapFactory.decodeStream(stream)?.let { bitmap ->
//                            plantDetector.processImage(bitmap)
//                        }
//                    }
                }
            }
            .addOnFailureListener {
                message.value = "Failure: ${it.message}"
            }
    }

    fun analiseImage(file: File) {
        BitmapFactory.decodeFile(file.absolutePath).let { bitmap ->
            plantDetector.processImage(bitmap).let {
                message.value = it
            }
        }
    }

    companion object {
        private var TAG = MainViewModel::class.java.name
    }
}
