package com.waracle.vision.toxicplants

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.waracle.vision.toxicplants.ui.theme.ToxicPlantsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ToxicPlantsTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    ToxicPlantScreen()
                }
            }
        }
    }
}

@Composable
fun ToxicPlantScreen(viewModel: MainViewModel = viewModel()) {

    val message by viewModel.message.collectAsStateWithLifecycle()

    ToxicPlantContent(message)
}

@Composable
fun ToxicPlantContent(message: String) {

    Text(modifier = Modifier.fillMaxWidth(), text = message)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ToxicPlantsTheme {
        ToxicPlantContent("Test!!")
    }
}