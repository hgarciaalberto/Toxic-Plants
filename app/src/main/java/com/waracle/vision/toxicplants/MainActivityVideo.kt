package com.waracle.vision.toxicplants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.waracle.vision.toxicplants.camera.video.FileManager
import com.waracle.vision.toxicplants.camera.video.PermissionsHandler
import com.waracle.vision.toxicplants.camera.video.RecordingScreen
import com.waracle.vision.toxicplants.camera.video.RecordingViewModel

class MainActivityVideo : ComponentActivity() {

    private val fileManager = FileManager(this)

    private val permissionsHandler = PermissionsHandler()

    @Suppress("UNCHECKED_CAST")
    private val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecordingViewModel::class.java)) {
                return RecordingViewModel(fileManager, permissionsHandler) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecordingScreen(factory = viewModelFactory) { message ->
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}