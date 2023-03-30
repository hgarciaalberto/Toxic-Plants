package com.waracle.vision.toxicplants.navigation

sealed class AppScreens(val route: String) {
    object AppList : AppScreens("AppList")

    object DetectPlantPicture : AppScreens("DetectPlantPicture")
    object DetectPlantVideo : AppScreens("DetectPlantVideo")
    object DetectModelObjects : AppScreens("DetectModelObjects")
    object OpenCVHelloWorld : AppScreens("OpenCVHelloWorld")
}
