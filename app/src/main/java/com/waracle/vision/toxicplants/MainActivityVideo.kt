package com.waracle.vision.toxicplants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.android.material.snackbar.Snackbar
import com.waracle.vision.toxicplants.camera.video.RecordingScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivityVideo : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecordingScreen { message ->
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
            }

        }
    }
}