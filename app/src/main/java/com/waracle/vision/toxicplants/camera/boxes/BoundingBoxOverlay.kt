package com.waracle.vision.toxicplants.camera.boxes

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.waracle.vision.toxicplants.objectdetector.ObjectDetectorProcessor

@Composable
fun BoundingBoxOverlay(
    modifier: Modifier = Modifier,
    boundingBoxes: List<ObjectDetectorProcessor.NormalizedRect>,
    boxColor: Color = Color.Red,
    boxStrokeWidth: Float = 2.dp.value
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        for (box in boundingBoxes) {
            drawRect(
                color = boxColor,
                topLeft = androidx.compose.ui.geometry.Offset(box.left * canvasWidth, box.top * canvasHeight),
                size = androidx.compose.ui.geometry.Size((box.right - box.left) * canvasWidth, (box.bottom - box.top) * canvasHeight),
                style = Stroke(width = boxStrokeWidth)
            )
        }
    }
}
