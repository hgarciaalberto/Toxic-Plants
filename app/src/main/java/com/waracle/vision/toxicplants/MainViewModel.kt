package com.waracle.vision.toxicplants

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.waracle.vision.toxicplants.camera.rotate
import com.waracle.vision.toxicplants.objectdetector.InfoMessage.Companion.toInfoMessage
import com.waracle.vision.toxicplants.objectdetector.Message
import com.waracle.vision.toxicplants.objectdetector.PlantDetectorOld
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val plantDetector: PlantDetectorOld
) : ViewModel() {

    val message = MutableStateFlow<List<Message>>(listOf("Waiting".toInfoMessage())).apply {
        combine(plantDetector.message) { it1, it2 -> it1 + it2 }
    }

    fun analiseImage(file: File) {
        viewModelScope.launch {
            BitmapFactory.decodeFile(file.absolutePath).let { bitmap ->
                plantDetector.processImage(bitmap.rotate()).let {
                    message.value = listOf(it)
                }
            }
        }
    }
}
