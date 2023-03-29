package com.waracle.vision.toxicplants.ui.features.plantsdetector.picture

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.waracle.vision.toxicplants.PermissionScreen
import com.waracle.vision.toxicplants.camera.rotate
import com.waracle.vision.toxicplants.ui.features.plantsdetector.video.RecordingViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalPermissionsApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun ToxicPlantCaptureScreen(
    navigation: NavController,
    modifier: Modifier = Modifier,
    viewModel: RecordingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val message by viewModel.message.collectAsStateWithLifecycle()

    PermissionScreen(modifier = modifier) { result ->

        if (result) {
            Column(modifier) {
                if (message.isNotBlank()) {
                    Text(message)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                CameraCapture(modifier) { file ->
                    Toast.makeText(context, "${file.absoluteFile}", Toast.LENGTH_SHORT).show()
                    BitmapFactory.decodeFile(file.absolutePath).let { bitmap ->
                        viewModel.plantDetector.processImage(bitmap.rotate())
                    }
                }
            }
        }
    }
}