package com.waracle.vision.toxicplants

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.waracle.vision.toxicplants.camera.capture.CameraCapture
import com.waracle.vision.toxicplants.objectdetector.Detector
import com.waracle.vision.toxicplants.objectdetector.InfoMessage
import com.waracle.vision.toxicplants.ui.theme.ToxicPlantsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToxicPlantsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    ToxicPlantScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun ToxicPlantScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val messages by viewModel.message.collectAsStateWithLifecycle()

    PermissionScreen(modifier = modifier) { result ->

        if (result) {
            Column(modifier) {
                if (messages.isNotEmpty()) {
                    for(message in messages) {
                        when(message) {
                            is InfoMessage -> {
                                Text(message.info)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            is Detector.DetectionResult.SUCCESS -> {
                                Text(message.label)
                                Text(message.confidence.toString())
                                //todo draw bounds
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            is Detector.DetectionResult.ERROR -> {
                                Text(message.reason)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
                CameraCapture(modifier) { file ->
                    Toast.makeText(context, "${file.absoluteFile}", Toast.LENGTH_SHORT).show()
                    viewModel.analiseImage(file)
                }
            }
        }
    }
}
