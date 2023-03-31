package com.waracle.vision.toxicplants.camera.boxes

import android.graphics.Rect
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Devices.PIXEL_4_XL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.waracle.vision.toxicplants.ui.features.plantsdetector.video.PreviewState
import com.waracle.vision.toxicplants.ui.theme.ToxicPlantsTheme

@Composable
fun BoundingBoxOverlay(
    modifier: Modifier = Modifier,
    boundingBoxes: List<Rect>,
    imageProxySize: android.util.Size,
    previewViewSize: android.util.Size,
    boxColor: Color = Color.Red,
    boxStrokeWidth: Float = 2.dp.value
) {
    Layout(
        modifier = modifier,
        content = {
            Canvas(
                modifier = Modifier.fillMaxSize(),
                onDraw = {
                    val scaleX = previewViewSize.width / imageProxySize.width
                    val scaleY = previewViewSize.height / imageProxySize.height
                    Log.d("test", "SCREEN SIZE: ${previewViewSize}." +
                            "\nIMAGE SIZE: ${imageProxySize}" +
                            "\nCANVAS SIZE: ${this.size}" +
                            "\nSCALE_X: ${scaleX}, SCALE_Y: ${scaleY}")

                    val offsetX = (size.width - imageProxySize.width * scaleX) / 2f
                    val offsetY = (size.height - imageProxySize.height * scaleY) / 2f

                    for (box in boundingBoxes) {
                        val scaledBox = Rect(
                            maxOf(0, (box.left * scaleX + offsetX).toInt()),
                            maxOf(0, (box.top * scaleY + offsetY).toInt()),
                            minOf((box.right * scaleX + offsetX).toInt(), size.width.toInt()),
                            minOf((box.bottom * scaleY + offsetY).toInt(), size.height.toInt())
                        )

                        Log.d("test", "Bounding Box ORIGINAL: $box" +
                                "\n Bounding Box SCALED: ${scaledBox}")

                        drawRect(
                            color = boxColor,
                            topLeft = Offset(scaledBox.left.toFloat(), scaledBox.top.toFloat()),
                            size = Size((scaledBox.right - scaledBox.left).toFloat(), (scaledBox.bottom - scaledBox.top).toFloat()),
                            style = Stroke(width = boxStrokeWidth)
                        )
                    }
                }
            )
        }
    ) { measurables, constraints ->
        // Measure the canvas with the given constraints
        val placeable = measurables.first().measure(constraints)

        // Return the layout size and placeable
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
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
            imageProxySize = PreviewState().size,
            previewViewSize = PreviewState().size,
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

@Composable
fun ObjectDetectionView(detectionResults: List<Rect>,
                        screenSize: Size) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Define the paint object to use for drawing the boxes
        val boxColor = Color.Red
        val boxStrokeWidth = 3.dp.toPx()
        val scaleX = size.width / screenSize.width
        val scaleY = size.height / screenSize.height

        // Draw boxes around each detected object
        detectionResults.forEach { rect ->
            val scaledRect = Rect(
                (rect.left * scaleX).toInt(),
                (rect.top * scaleY).toInt(),
                (rect.right * scaleX).toInt(),
                (rect.bottom * scaleY).toInt()
            )
            drawRect(
                color = boxColor,
                topLeft = Offset(scaledRect.left.toFloat(), scaledRect.top.toFloat()),
                size = Size(scaledRect.width().toFloat(), scaledRect.height().toFloat()),
                style = Stroke(width = boxStrokeWidth)
            )
        }
    }
}


@Preview(showSystemUi = true, showBackground = true, device = PIXEL_4_XL)
@Composable
fun ObjectDetectionViewPreview() {
    ToxicPlantsTheme {
        ObjectDetectionView(
            detectionResults = arrayListOf(
                Rect(307, 490 ,412, 698)
            ),
            Size(1080f, 2400f)
        )
    }
}