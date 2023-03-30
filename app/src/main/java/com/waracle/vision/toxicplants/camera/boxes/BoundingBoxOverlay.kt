package com.waracle.vision.toxicplants.camera.boxes

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun BoundingBoxOverlay(
    modifier: Modifier = Modifier,
    boundingBoxes: List<Rect>,
    imageProxySize: Size,
    previewSize: Size,
    boxColor: Color = Color.Red,
    boxStrokeWidth: Float = 2.dp.value
) {
    Canvas(modifier = modifier) {
        val minScale = minOf(previewSize.width / imageProxySize.width, previewSize.height / imageProxySize.height)
        val offsetX = (size.width - imageProxySize.width * minScale) / 2f
        val offsetY = (size.height - imageProxySize.height * minScale) / 2f

        for (box in boundingBoxes) {
            val scaledBox = Rect(
                (box.left * minScale + offsetX).toInt(),
                (box.top * minScale + offsetY).toInt(),
                (box.right * minScale + offsetX).toInt(),
                (box.bottom * minScale + offsetY).toInt()
            )

            drawRect(
                color = boxColor,
                topLeft = Offset(scaledBox.left.toFloat(), scaledBox.top.toFloat()),
                size = Size((scaledBox.right - scaledBox.left).toFloat(), (scaledBox.bottom - scaledBox.top).toFloat()),
                style = Stroke(width = boxStrokeWidth)
            )
        }
    }
}
