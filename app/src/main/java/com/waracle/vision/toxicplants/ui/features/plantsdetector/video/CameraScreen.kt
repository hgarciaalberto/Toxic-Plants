package com.waracle.vision.toxicplants.ui.features.plantsdetector.video

import android.Manifest
import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.camera.core.CameraInfo
import androidx.camera.core.TorchState
import androidx.camera.core.TorchState.OFF
import androidx.camera.core.TorchState.ON
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.waracle.vision.toxicplants.R
import com.waracle.vision.toxicplants.ui.features.utils.CaptureType
import com.waracle.vision.toxicplants.ui.theme.ToxicPlantsTheme
import timber.log.Timber
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun CameraScreen(
    navigation: NavController,
    captureType: CaptureType,
    cameraViewModel: CameraViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val state by cameraViewModel.state.collectAsState()
    val permissionMessage by cameraViewModel.permissionMessage.collectAsStateWithLifecycle()
    val detectionMessage by cameraViewModel.plantDetector.message.collectAsStateWithLifecycle()

    val listener = remember(cameraViewModel) {
        object : CameraCaptureManager.Listener {
            override fun onInitialised(cameraLensInfo: HashMap<Int, CameraInfo>) {
                cameraViewModel.onEvent(CameraViewModel.Event.CameraInitialized(cameraLensInfo))
            }

            override fun recordingPaused() {
                cameraViewModel.onEvent(CameraViewModel.Event.RecordingPaused)
            }

            override fun onProgress(progress: Int) {
                cameraViewModel.onEvent(CameraViewModel.Event.OnProgress(progress))
            }

            override fun recordingCompleted(outputUri: Uri) {
                cameraViewModel.onEvent(CameraViewModel.Event.RecordingEnded(outputUri))
            }

            override fun onError(throwable: Throwable?) {
                cameraViewModel.onEvent(CameraViewModel.Event.Error(throwable))
            }

            override fun onProcessFrame(bitmap: Bitmap?) {
                bitmap?.let { cameraViewModel.analiseImage(it) }
            }

            override fun onTakePicture(bitmap: Bitmap?) {
                bitmap?.let { cameraViewModel.analiseImage(bitmap) }
            }
        }
    }

    val captureManager = remember(cameraViewModel) {
        CameraCaptureManager.Builder(context)
            .registerLifecycleOwner(lifecycleOwner)
            .setCaptureMode(captureType)
            .create()
            .apply { this.listener = listener }
    }

    val permissions = remember { listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) }
    HandlePermissionsRequest(permissions = permissions, permissionsHandler = cameraViewModel.permissionsHandler)

    CompositionLocalProvider(LocalCameraCaptureManager provides captureManager) {
        CameraContent(
            message = if (permissionMessage.isNotBlank())
                "$permissionMessage"
            else
                "$detectionMessage",
            captureType = captureType,
            allPermissionsGranted = state.multiplePermissionsState?.allPermissionsGranted ?: false,
            cameraLens = state.lens,
            torchState = state.torchState,
            hasFlashUnit = state.lensInfo[state.lens]?.hasFlashUnit() ?: false,
            hasDualCamera = state.lensInfo.size > 1,
            recordedLength = state.recordedLength,
            recordingStatus = state.recordingStatus,
            onEvent = cameraViewModel::onEvent
        )
    }

    LaunchedEffect(cameraViewModel) {
        cameraViewModel.effect.collect {
            when (it) {
                is CameraViewModel.Effect.NavigateBack -> navigation.navigateUp()
                is CameraViewModel.Effect.ShowMessage -> {
                    Timber.i("message: ${it.message}")
                }
                is CameraViewModel.Effect.RecordVideo -> captureManager.startRecording(it.filePath)
                is CameraViewModel.Effect.SavePicture -> captureManager.savePicture()
                CameraViewModel.Effect.PauseRecording -> captureManager.pauseRecording()
                CameraViewModel.Effect.ResumeRecording -> captureManager.resumeRecording()
                CameraViewModel.Effect.StopRecording -> captureManager.stopRecording()
            }
        }
    }
}

@Composable
private fun CameraContent(
    message: String,
    captureType: CaptureType,
    allPermissionsGranted: Boolean,
    cameraLens: Int?,
    @TorchState.State torchState: Int,
    hasFlashUnit: Boolean,
    hasDualCamera: Boolean,
    recordedLength: Int,
    recordingStatus: CameraViewModel.RecordingStatus,
    onEvent: (CameraViewModel.Event) -> Unit
) {
    if (!allPermissionsGranted) {
        RequestPermission(message = R.string.request_permissions) {
            onEvent(CameraViewModel.Event.PermissionRequired)
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {

            cameraLens?.let {
                CameraPreview(lens = it, torchState = torchState)
                if (recordingStatus == CameraViewModel.RecordingStatus.Idle) {
                    CaptureHeader(
                        modifier = Modifier.align(Alignment.TopStart),
                        message = message,
                        showFlashIcon = hasFlashUnit,
                        torchState = torchState,
                        onFlashTapped = { onEvent(CameraViewModel.Event.FlashTapped) },
                        onCloseTapped = { onEvent(CameraViewModel.Event.CloseTapped) }
                    )
                }
                if (recordedLength > 0) {
                    Timer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        seconds = recordedLength,
                        message = message
                    )
                }

                if (captureType == CaptureType.IMAGE) {

                    CameraIcon(
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.BottomCenter)
                            .padding(20.dp, bottom = 40.dp),
                        onTapped = { onEvent(CameraViewModel.Event.PictureTapped) }
                    )

//                RecordFooter(
//                    captureType = captureType,
//                    modifier = Modifier.align(Alignment.BottomStart),
//                    recordingStatus = recordingStatus,
//                    showFlipIcon = hasDualCamera,
//                    onRecordTapped = { onEvent(CameraViewModel.Event.RecordTapped) },
//                    onPictureTapped = { onEvent(CameraViewModel.Event.PictureTapped) },
//                    onPauseTapped = { onEvent(CameraViewModel.Event.PauseTapped) },
//                    onResumeTapped = { onEvent(CameraViewModel.Event.ResumeTapped) },
//                    onStopTapped = { onEvent(CameraViewModel.Event.StopTapped) },
//                    onFlipTapped = { onEvent(CameraViewModel.Event.FlipTapped) }
//                )
                }
            }
        }
    }
}

@Composable
internal fun CaptureHeader(
    message: String,
    modifier: Modifier = Modifier,
    showFlashIcon: Boolean,
    torchState: Int,
    onFlashTapped: () -> Unit,
    onCloseTapped: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
            .then(modifier)
    ) {
        if (showFlashIcon) {
            CameraTorchIcon(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp),
                torchState = torchState,
                onTapped = onFlashTapped
            )
        }
        Text(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .align(Alignment.CenterVertically)
                .padding(10.dp, 0.dp, 10.dp, 0.dp)
                .background(Color.White),
            text = message,
            textAlign = TextAlign.Center,
            fontSize = 20.sp
        )
        CameraCloseIcon(
            modifier = Modifier
                .wrapContentSize()
                .wrapContentHeight()
                .padding(8.dp),
            onTapped = onCloseTapped,
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0)
@Composable
fun CaptureHeaderPreviewFlashOn() {
    ToxicPlantsTheme {
        CaptureHeader(
            message = "Message",
            showFlashIcon = true,
            torchState = ON,
            onFlashTapped = {},
            onCloseTapped = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0)
@Composable
fun CaptureHeaderPreviewFlashOff() {
    ToxicPlantsTheme {
        CaptureHeader(
            message = "Message",
            showFlashIcon = true,
            torchState = OFF,
            onFlashTapped = {},
            onCloseTapped = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0)
@Composable
fun CaptureHeaderPreviewNoFlash() {
    ToxicPlantsTheme {
        CaptureHeader(
            message = "Message",
            showFlashIcon = false,
            torchState = OFF,
            onFlashTapped = {},
            onCloseTapped = {}
        )
    }
}


@Composable
internal fun RecordFooter(
    captureType: CaptureType,
    modifier: Modifier = Modifier,
    recordingStatus: CameraViewModel.RecordingStatus,
    showFlipIcon: Boolean,
    onRecordTapped: () -> Unit,
    onPictureTapped: () -> Unit,
    onStopTapped: () -> Unit,
    onPauseTapped: () -> Unit,
    onResumeTapped: () -> Unit,
    onFlipTapped: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 24.dp)
            .then(modifier)
    ) {
        when (recordingStatus) {
            CameraViewModel.RecordingStatus.Idle -> {
                CameraRecordIcon(
                    modifier = Modifier.align(Alignment.Center),
                    onTapped = when (captureType) {
                        CaptureType.VIDEO -> onRecordTapped
                        CaptureType.IMAGE -> onPictureTapped
                    }
                )
            }
            CameraViewModel.RecordingStatus.Paused -> {
                CameraStopIcon(
                    modifier = Modifier.align(Alignment.Center),
                    onTapped = onStopTapped
                )
                CameraPlayIconSmall(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(end = 150.dp),
                    onTapped = onResumeTapped
                )
            }
            CameraViewModel.RecordingStatus.InProgress -> {
                CameraStopIcon(
                    modifier = Modifier.align(Alignment.Center),
                    onTapped = onStopTapped
                )
                CameraPauseIconSmall(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(end = 140.dp),
                    onTapped = onPauseTapped
                )
            }
        }

        if (showFlipIcon && recordingStatus == CameraViewModel.RecordingStatus.Idle) {
            CameraFlipIcon(
                modifier = Modifier.align(Alignment.CenterEnd),
                onTapped = onFlipTapped
            )
        }
    }
}

@Composable
private fun CameraPreview(
    lens: Int,
    @TorchState.State torchState: Int
) {
    val captureManager = LocalCameraCaptureManager.current
    BoxWithConstraints {
        AndroidView(
            factory = {
                captureManager.showPreview(
                    PreviewState(
                        cameraLens = lens,
                        torchState = torchState,
                        size = Size(this.minWidth.value.toInt(), this.maxHeight.value.toInt()),
                    )
                )
            },
            modifier = Modifier.fillMaxSize(),
            update = {
                captureManager.updatePreview(
                    PreviewState(cameraLens = lens, torchState = torchState),
                    it
                )
            }
        )
    }
}