package com.waracle.vision.toxicplants

import android.content.Context
import android.graphics.*
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)

suspend fun ImageCapture.takePicture(executor: Executor): File {
    val photoFile = withContext(Dispatchers.IO) {
        kotlin.runCatching {
            File.createTempFile("image", ".jpg").also {
                Log.d("TakePicture", "${it.absoluteFile}")
            }
        }.getOrElse { ex ->
            Log.e("TakePicture", "Failed to create temporary file", ex)
            File("/dev/null")
        }
    }

    return suspendCoroutine { continuation ->
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        takePicture(outputOptions, executor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                continuation.resume(photoFile)
            }

            override fun onError(ex: ImageCaptureException) {
                Log.e("TakePicture", "Image capture failed", ex)
                continuation.resumeWithException(ex)
            }
        })
    }
}

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener(
            {
                continuation.resume(future.get())
            },
            executor
        )
    }
}

fun Bitmap.rotate(degrees: Float = 90f): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)

    val rotatedBitmap = Bitmap.createBitmap(
        this, 0, 0, this.width, this.height, matrix, true
    )

    this.recycle()

    return rotatedBitmap
}

fun ImageProxy.toBitmap(): Bitmap? {
    val nv21Buffer = planes[0].buffer
    val ySize = nv21Buffer.remaining()
    val uvSize = planes[1].buffer.remaining() + planes[2].buffer.remaining()
    val bufferSize = ySize + uvSize

    val data = ByteArray(bufferSize)
    nv21Buffer.get(data, 0, ySize)

    val uvBuffer = planes[2].buffer.duplicate()
    val uv = ByteArray(uvSize)
    uvBuffer.get(uv, 0, planes[2].buffer.remaining())
    var i = 0
    while (i < uvSize - 1) {
        data[ySize + i] = uv[i + 1]
        data[ySize + i + 1] = uv[i]
        i += 2
    }

    val yuvImage = YuvImage(data, ImageFormat.NV21, width, height, null)
    val outputStream = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, outputStream)
    val jpegByteArray = outputStream.toByteArray()

    return BitmapFactory.decodeByteArray(jpegByteArray, 0, jpegByteArray.size)
}