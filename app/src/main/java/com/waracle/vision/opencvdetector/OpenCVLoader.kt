package com.waracle.vision.opencvdetector

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import timber.log.Timber
import javax.inject.Inject

class OpenCVLoader @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "OpenCVLoader"
    }

    private var onLoadedCallback: ((Boolean) -> Unit)? = null
    private val loaderCallback = object : BaseLoaderCallback(context) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Timber.i("OpenCV loaded successfully")
                    onLoadedCallback?.invoke(true)
                }
                else -> {
                    super.onManagerConnected(status)
                    Timber.e("OpenCV could not be loaded")
                    onLoadedCallback?.invoke(false)
                }
            }
        }
    }

    fun loadOpenCV(onLoaded: ((isLoaded: Boolean) -> Unit)) {
        this.onLoadedCallback = onLoaded
        if (!OpenCVLoader.initDebug()) {
            Timber.d("Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, context, loaderCallback)
        } else {
            Timber.d("OpenCV library found inside package. Using it!")
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }
}
