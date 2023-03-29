package com.waracle.vision.toxicplants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.waracle.vision.toxicplants.navigation.AppNavigation
import com.waracle.vision.toxicplants.ui.theme.ToxicPlantsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToxicPlantsTheme {
                AppNavigation()
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun DefaultPreview() {
    ToxicPlantsTheme {
        AppNavigation()
    }
}