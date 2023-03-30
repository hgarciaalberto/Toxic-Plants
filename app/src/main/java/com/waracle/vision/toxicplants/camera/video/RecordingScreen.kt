package com.waracle.vision.toxicplants.camera.video

import android.Manifest
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import androidx.camera.core.CameraInfo
import androidx.camera.core.ImageProxy
import androidx.camera.core.TorchState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.waracle.vision.toxicplants.R
import com.waracle.vision.toxicplants.camera.boxes.BoundingBoxOverlay
import com.waracle.vision.toxicplants.objectdetector.Message
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun RecordingScreen(
    recordingViewModel: RecordingViewModel = hiltViewModel(),
    onShowMessage: (message: Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val state by recordingViewModel.state.collectAsState()
    val message by recordingViewModel.message.collectAsStateWithLifecycle()
    val boundingBoxes by recordingViewModel.boundingBoxes.collectAsStateWithLifecycle()
    var imageProxySize by remember { mutableStateOf(Size(0f, 0f)) }

    val listener = remember(recordingViewModel) {
        object : VideoCaptureManager.Listener {
            override fun onInitialised(cameraLensInfo: HashMap<Int, CameraInfo>) {
                recordingViewModel.onEvent(RecordingViewModel.Event.CameraInitialized(cameraLensInfo))
            }

            override fun recordingPaused() {
                recordingViewModel.onEvent(RecordingViewModel.Event.RecordingPaused)
            }

            override fun onProgress(progress: Int) {
                recordingViewModel.onEvent(RecordingViewModel.Event.OnProgress(progress))
            }

            override fun recordingCompleted(outputUri: Uri) {
                recordingViewModel.onEvent(RecordingViewModel.Event.RecordingEnded(outputUri))
            }

            override fun onError(throwable: Throwable?) {
                recordingViewModel.onEvent(RecordingViewModel.Event.Error(throwable))
            }

            override fun process(imageProxy: ImageProxy) {
                imageProxySize = Size(imageProxy.width.toFloat(), imageProxy.height.toFloat())
                recordingViewModel.analiseImage(imageProxy)
            }
        }
    }

    val captureManager = remember(recordingViewModel) {
        VideoCaptureManager.Builder(context)
            .registerLifecycleOwner(lifecycleOwner)
            .create()
            .apply { this.listener = listener }
    }

    val permissions =
        remember { listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO) }
    HandlePermissionsRequest(
        permissions = permissions,
        permissionsHandler = recordingViewModel.permissionsHandler
    )

    CompositionLocalProvider(LocalVideoCaptureManager provides captureManager) {
        VideoScreenContent(
            message = message,
            imageProxySize = imageProxySize,
            allPermissionsGranted = state.multiplePermissionsState?.allPermissionsGranted ?: false,
            cameraLens = state.lens,
            torchState = state.torchState,
            hasFlashUnit = state.lensInfo[state.lens]?.hasFlashUnit() ?: false,
            hasDualCamera = state.lensInfo.size > 1,
            recordedLength = state.recordedLength,
            recordingStatus = state.recordingStatus,
            onEvent = recordingViewModel::onEvent,
            boundingBoxes = boundingBoxes
        )
    }

    LaunchedEffect(recordingViewModel) {
        recordingViewModel.effect.collect {
            when (it) {
//                is RecordingViewModel.Effect.NavigateTo -> navController.navigateTo(it.route)
                is RecordingViewModel.Effect.ShowMessage -> onShowMessage(it.message)
                is RecordingViewModel.Effect.RecordVideo -> captureManager.startRecording(it.filePath)
                RecordingViewModel.Effect.PauseRecording -> captureManager.pauseRecording()
                RecordingViewModel.Effect.ResumeRecording -> captureManager.resumeRecording()
                RecordingViewModel.Effect.StopRecording -> captureManager.stopRecording()
            }
        }
    }
}

@Composable
private fun VideoScreenContent(
    message: Message,
    imageProxySize: Size,
    boundingBoxes: List<Rect>,
    allPermissionsGranted: Boolean,
    cameraLens: Int?,
    @TorchState.State torchState: Int,
    hasFlashUnit: Boolean,
    hasDualCamera: Boolean,
    recordedLength: Int,
    recordingStatus: RecordingViewModel.RecordingStatus,
    onEvent: (RecordingViewModel.Event) -> Unit
) {

    var previewSize by remember { mutableStateOf(Size(0f, 0f)) }
    if (!allPermissionsGranted) {
        RequestPermission(message = R.string.request_permissions) {
            onEvent(RecordingViewModel.Event.PermissionRequired)
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {

            cameraLens?.let {
                CameraPreview(lens = it, torchState = torchState){ newSize ->
                    previewSize = newSize
                }
                if (recordingStatus == RecordingViewModel.RecordingStatus.Idle) {
                    CaptureHeader(
                        modifier = Modifier.align(Alignment.TopStart),
                        showFlashIcon = hasFlashUnit,
                        torchState = torchState,
                        onFlashTapped = { onEvent(RecordingViewModel.Event.FlashTapped) },
                        onCloseTapped = { onEvent(RecordingViewModel.Event.CloseTapped) }
                    )
                }

                BoundingBoxOverlay(
                    boundingBoxes = boundingBoxes,
                    modifier = Modifier.fillMaxSize(),
                    imageProxySize = imageProxySize,
                    previewSize = previewSize
                )

                if (recordedLength > 0) {
                    Timer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        seconds = recordedLength
                    )
                }

                Text(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.White)
                        .fillMaxWidth()
                        .padding(10.dp, 10.dp, 10.dp, 15.dp),
                    text = message.toString(),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
        }
    }
}

fun Density.toPx(dp: Dp): Float {
    return dp.value * density
}


@Composable
internal fun CaptureHeader(
    modifier: Modifier = Modifier,
    showFlashIcon: Boolean,
    torchState: Int,
    onFlashTapped: () -> Unit,
    onCloseTapped: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(8.dp)
            .then(modifier)
    ) {
        if (showFlashIcon) {
            CameraTorchIcon(torchState = torchState, onTapped = onFlashTapped)
        }
        CameraCloseIcon(onTapped = onCloseTapped, modifier = Modifier.align(Alignment.TopEnd))
    }
}


@Composable
internal fun RecordFooter(
    modifier: Modifier = Modifier,
    recordingStatus: RecordingViewModel.RecordingStatus,
    showFlipIcon: Boolean,
    onRecordTapped: () -> Unit,
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
            RecordingViewModel.RecordingStatus.Idle -> {
                CameraRecordIcon(
                    modifier = Modifier.align(Alignment.Center),
                    onTapped = onRecordTapped
                )
            }
            RecordingViewModel.RecordingStatus.Paused -> {
                CameraStopIcon(modifier = Modifier.align(Alignment.Center), onTapped = onStopTapped)
                CameraPlayIconSmall(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(end = 150.dp), onTapped = onResumeTapped
                )
            }
            RecordingViewModel.RecordingStatus.InProgress -> {
                CameraStopIcon(modifier = Modifier.align(Alignment.Center), onTapped = onStopTapped)
                CameraPauseIconSmall(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(end = 140.dp), onTapped = onPauseTapped
                )
            }
        }

        if (showFlipIcon && recordingStatus == RecordingViewModel.RecordingStatus.Idle) {
            CameraFlipIcon(modifier = Modifier.align(Alignment.CenterEnd), onTapped = onFlipTapped)
        }
    }
}

@Composable
private fun CameraPreview(lens: Int,
                          @TorchState.State torchState: Int,
                          onSizeChanged: (Size) -> Unit) {
    val captureManager = LocalVideoCaptureManager.current
    BoxWithConstraints {
        val size = Size(this.minWidth.value, this.maxHeight.value)
        onSizeChanged(size)
        AndroidView(
            factory = {
                captureManager.showPreview(
                    PreviewState(
                        cameraLens = lens,
                        torchState = torchState,
                        size = android.util.Size(this.minWidth.value.toInt(), this.maxHeight.value.toInt()),
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