package com.waracle.vision.toxicplants.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.waracle.vision.toxicplants.ui.features.dashboard.AppList
import com.waracle.vision.toxicplants.ui.features.plantsdetector.video.CameraScreen
import com.waracle.vision.toxicplants.ui.features.utils.CaptureType

@Composable
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppScreens.AppList.route) {
        composable(route = AppScreens.AppList.route) {
            AppList(navController)
        }
        composable(route = AppScreens.DetectPlantPicture.route) {
            CameraScreen(navController, CaptureType.IMAGE)
        }
        composable(route = AppScreens.DetectPlantVideo.route) {
            CameraScreen(navController, CaptureType.VIDEO)
        }
        composable(route = AppScreens.DetectModelObjects.route) {
            CameraScreen(navController, CaptureType.BOUNDARY_OBJECT_TFLite)
        }
        composable(route = AppScreens.OpenCVHelloWorld.route) {
            CameraScreen(navController, CaptureType.BOUNDARY_OBJECT_OpenCV)
        }
        composable(route = AppScreens.DetectObjectsCncd.route) {
            CameraScreen(navController, CaptureType.BOUNDARY_OBJECT_Cncd)
        }
    }
}
