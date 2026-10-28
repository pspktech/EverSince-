package com.eversince

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import eversince.ui.screens.HomeScreen
import eversince.ui.theme.EverSinceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EverSinceTheme {
                HomeScreen()
            }
        }
    }
}
