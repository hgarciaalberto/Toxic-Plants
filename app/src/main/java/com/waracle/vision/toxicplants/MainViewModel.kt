package com.waracle.vision.toxicplants

import android.app.Application
import android.graphics.BitmapFactory
import androidx.lifecycle.AndroidViewModel
import com.waracle.vision.toxicplants.camera.rotate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.io.File


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val plantDetector = PlantDetector()

    val message = MutableStateFlow("Waiting").apply {
        combine(plantDetector.message) { it1, it2 -> it1 + it2 }
    }

    fun analiseImage(file: File) {
        BitmapFactory.decodeFile(file.absolutePath).let { bitmap ->
            plantDetector.processImage(bitmap.rotate()).let {
                message.value = it
            }
        }
    }

    companion object {
        private var TAG = MainViewModel::class.java.name
    }
}
