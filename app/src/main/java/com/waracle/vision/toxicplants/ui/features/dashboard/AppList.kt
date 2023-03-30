package com.waracle.vision.toxicplants.ui.features.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.waracle.vision.toxicplants.navigation.AppScreens
import com.waracle.vision.toxicplants.ui.theme.ToxicPlantsTheme

@Composable
fun AppList(navController: NavController) {
    Column(modifier = Modifier.padding(20.dp)) {
        ItemList(
            "Detect Plants on Picture",
            onClick = {
                navController.navigate(AppScreens.DetectPlantPicture.route)
            }
        )
        ItemList(
            "Detect Plant on Video",
            onClick = {
                navController.navigate(AppScreens.DetectPlantVideo.route)
            }
        )
//        ItemList(
//            "Detect Object from Model",
//            onClick = {
//                navController.navigate(AppScreens.DetectModelObjects.route)
//            }
//        )
//        ItemList(
//            "OpenCV Hello World",
//            onClick = {
//                navController.navigate(AppScreens.OpenCVHelloWorld.route)
//            }
//        )
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun DefaultPreviewAppList() {
    ToxicPlantsTheme {
        val navController = rememberNavController()
        AppList(navController)
    }
}
