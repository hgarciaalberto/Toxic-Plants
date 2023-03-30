package com.waracle.vision.toxicplants.camera.boxes

import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Devices.PIXEL_4_XL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.waracle.vision.toxicplants.ui.theme.ToxicPlantsTheme

@Composable
fun BoundingBoxOverlay(
    modifier: Modifier = Modifier,
    boundingBoxes: List<Rect>,
    imageProxySize: Size,
    previewSize: Size,
    boxColor: Color = Color.Red,
    boxStrokeWidth: Float = 2.dp.value
) {
    Log.d("test", "SCREEN SIZE: ${previewSize}")
    Canvas(modifier = modifier) {
        val scaleX = previewSize.width / imageProxySize.width
        val scaleY = previewSize.height / imageProxySize.height

        val offsetX = (size.width - imageProxySize.width * scaleX) / 2f
        val offsetY = (size.height - imageProxySize.height * scaleY) / 2f

        for (box in boundingBoxes) {
            val scaledBox = Rect(
                (box.left * scaleX + offsetX).toInt(),
                (box.top * scaleY + offsetY).toInt(),
                (box.right * scaleX + offsetX).toInt(),
                (box.bottom * scaleY + offsetY).toInt()
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

@Preview(showSystemUi = true, showBackground = true, device = PIXEL_4_XL)
@Composable
fun BoundingBoxOverlayPreview() {
    ToxicPlantsTheme {
        BoundingBoxOverlay(
            boundingBoxes = arrayListOf(
                Rect(Rect(307, 490 ,412, 698))
            ),
            imageProxySize = Size(360f, 780f),
            previewSize = Size(1080f, 2400f),
        )
    }
}

@Composable
fun RectanglesOverlay(
    modifier: Modifier = Modifier,
    boundingBoxes: List<Rect>,
    boxColor: Color = Color.Red,
    boxStrokeWidth: Float = 2.dp.value
) {
    Canvas(modifier = modifier) {
        for (box in boundingBoxes) {
            Log.d("test", "draw RECT: ${box}")
            drawRect(
                color = boxColor,
                topLeft = Offset(box.left.toFloat(), box.top.toFloat()),
                size = Size(box.width().toFloat(), box.height().toFloat()),
                style = Stroke(width = boxStrokeWidth)
            )
        }
    }
}


@Preview(showSystemUi = true, showBackground = true, device = PIXEL_4_XL)
@Composable
fun RectanglesOverlayPreview() {
    ToxicPlantsTheme {
        RectanglesOverlay(
            boundingBoxes = arrayListOf(
                Rect(307, 490 ,412, 698)
            )
        )
    }
}