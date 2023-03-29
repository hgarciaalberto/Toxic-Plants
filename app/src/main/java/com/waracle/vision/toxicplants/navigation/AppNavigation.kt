package com.waracle.vision.toxicplants.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.waracle.vision.toxicplants.camera.navigation.AppList
import com.waracle.vision.toxicplants.ui.features.plantsdetector.picture.ToxicPlantCaptureScreen
import com.waracle.vision.toxicplants.ui.features.plantsdetector.video.RecordingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppScreens.AppList.route) {
        composable(route = AppScreens.AppList.route) {
            AppList(navController)
        }

        composable(route = AppScreens.DetectPlantPicture.route) {
            ToxicPlantCaptureScreen(navController)
        }
        composable(route = AppScreens.DetectPlantVideo.route) {
            RecordingScreen(navController)
        }

        composable(route = AppScreens.DetectModelObjects.route) {
        }

        composable(route = AppScreens.OpenCVHelloWorld.route) {
        }
    }
}
