package com.waracle.vision.toxicplants

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import com.waracle.vision.toxicplants.camera.rotate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val plantDetector: PlantDetector
) : ViewModel() {

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
}
