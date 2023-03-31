/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.waracle.vision.googledemodetection.googledemodetection.objectdetection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import com.google.android.gms.common.annotation.KeepName
import com.google.mlkit.common.model.LocalModel
import com.waracle.vision.googledemodetection.googledemodetection.objectdetection.preference.PreferenceUtils
import com.waracle.vision.toxicplants.R
import com.waracle.vision.googledemodetection.googledemodetection.objectdetection.objectdetector.ObjectDetectorProcessor
import java.io.IOException

/** Live preview demo for ML Kit APIs. */
@KeepName
class LivePreviewActivity :
  AppCompatActivity(), OnItemSelectedListener, CompoundButton.OnCheckedChangeListener {

  private var cameraSource: CameraSource? = null
  private var preview: CameraSourcePreview? = null
  private var graphicOverlay: GraphicOverlay? = null
  private var selectedModel = OBJECT_DETECTION_CUSTOM

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Log.d(TAG, "onCreate")
    setContentView(R.layout.activity_vision_live_preview)

    preview = findViewById(R.id.preview_view)
    if (preview == null) {
      Log.d(TAG, "Preview is null")
    }

    graphicOverlay = findViewById(R.id.graphic_overlay)
    if (graphicOverlay == null) {
      Log.d(TAG, "graphicOverlay is null")
    }

    val facingSwitch = findViewById<ToggleButton>(R.id.facing_switch)
    facingSwitch.setOnCheckedChangeListener(this)

    createCameraSource(selectedModel)
  }

  @Synchronized
  override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
    // An item was selected. You can retrieve the selected item using
    // parent.getItemAtPosition(pos)
    selectedModel = parent?.getItemAtPosition(pos).toString()
    Log.d(TAG, "Selected model: $selectedModel")
    preview?.stop()
    createCameraSource(selectedModel)
    startCameraSource()
  }

  override fun onNothingSelected(parent: AdapterView<*>?) {
    // Do nothing.
  }

  override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
    Log.d(TAG, "Set facing")
    if (cameraSource != null) {
      if (isChecked) {
        cameraSource?.setFacing(CameraSource.CAMERA_FACING_FRONT)
      } else {
        cameraSource?.setFacing(CameraSource.CAMERA_FACING_BACK)
      }
    }
    preview?.stop()
    startCameraSource()
  }

  private fun createCameraSource(model: String) {
    // If there's no existing cameraSource, create one.
    if (cameraSource == null) {
      cameraSource = CameraSource(this, graphicOverlay)
    }
    try {
      when (model) {
        OBJECT_DETECTION -> {
          Log.i(TAG, "Using Object Detector Processor")
          val objectDetectorOptions = PreferenceUtils.getObjectDetectorOptionsForLivePreview(this)
          cameraSource!!.setMachineLearningFrameProcessor(
            ObjectDetectorProcessor(this, objectDetectorOptions)
          )
        }
        OBJECT_DETECTION_CUSTOM -> {
          Log.i(TAG, "Using Custom Object Detector Processor")
          val localModel =
            LocalModel.Builder().setAssetFilePath("lite-model_aiy_vision_classifier_plants_V1_3.tflite").build()
          val customObjectDetectorOptions =
            PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(this, localModel)
          cameraSource!!.setMachineLearningFrameProcessor(
            ObjectDetectorProcessor(this, customObjectDetectorOptions)
          )
        }
        CUSTOM_AUTOML_OBJECT_DETECTION -> {
          Log.i(TAG, "Using Custom AutoML Object Detector Processor")
          val customAutoMLODTLocalModel =
            LocalModel.Builder().setAssetManifestFilePath("automl/manifest.json").build()
          val customAutoMLODTOptions =
            PreferenceUtils.getCustomObjectDetectorOptionsForLivePreview(
              this,
              customAutoMLODTLocalModel
            )
          cameraSource!!.setMachineLearningFrameProcessor(
            ObjectDetectorProcessor(this, customAutoMLODTOptions)
          )
        }
        else -> Log.e(TAG, "Unknown model: $model")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Can not create image processor: $model", e)
      Toast.makeText(
          applicationContext,
          "Can not create image processor: " + e.message,
          Toast.LENGTH_LONG
        )
        .show()
    }
  }

  /**
   * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
   * (e.g., because onResume was called before the camera source was created), this will be called
   * again when the camera source is created.
   */
  private fun startCameraSource() {
    if (cameraSource != null) {
      try {
        if (preview == null) {
          Log.d(TAG, "resume: Preview is null")
        }
        if (graphicOverlay == null) {
          Log.d(TAG, "resume: graphOverlay is null")
        }
        preview!!.start(cameraSource, graphicOverlay)
      } catch (e: IOException) {
        Log.e(TAG, "Unable to start camera source.", e)
        cameraSource!!.release()
        cameraSource = null
      }
    }
  }

  public override fun onResume() {
    super.onResume()
    Log.d(TAG, "onResume")
    createCameraSource(selectedModel)
    startCameraSource()
  }

  /** Stops the camera. */
  override fun onPause() {
    super.onPause()
    preview?.stop()
  }

  public override fun onDestroy() {
    super.onDestroy()
    if (cameraSource != null) {
      cameraSource?.release()
    }
  }

  companion object {
    private const val OBJECT_DETECTION = "Object Detection"
    private const val OBJECT_DETECTION_CUSTOM = "Custom Object Detection"
    private const val CUSTOM_AUTOML_OBJECT_DETECTION = "Custom AutoML Object Detection (Flower)"
    private const val FACE_DETECTION = "Face Detection"
    private const val TEXT_RECOGNITION_LATIN = "Text Recognition Latin"
    private const val TEXT_RECOGNITION_CHINESE = "Text Recognition Chinese (Beta)"
    private const val TEXT_RECOGNITION_DEVANAGARI = "Text Recognition Devanagari (Beta)"
    private const val TEXT_RECOGNITION_JAPANESE = "Text Recognition Japanese (Beta)"
    private const val TEXT_RECOGNITION_KOREAN = "Text Recognition Korean (Beta)"
    private const val BARCODE_SCANNING = "Barcode Scanning"
    private const val IMAGE_LABELING = "Image Labeling"
    private const val IMAGE_LABELING_CUSTOM = "Custom Image Labeling (Birds)"
    private const val CUSTOM_AUTOML_LABELING = "Custom AutoML Image Labeling (Flower)"
    private const val POSE_DETECTION = "Pose Detection"
    private const val SELFIE_SEGMENTATION = "Selfie Segmentation"
    private const val FACE_MESH_DETECTION = "Face Mesh Detection (Beta)";

    private const val TAG = "LivePreviewActivity"
  }
}
