package com.eversince

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eversince.ui.screens.HomeScreen
import com.eversince.ui.theme.EverSinceTheme
import com.eversince.viewmodel.EverSinceViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EverSinceTheme {
                val vm: EverSinceViewModel = viewModel()
                val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { vm.savePartnerPhoto(it.toString()) } }
                HomeScreen(vm, onPickPartnerPhoto = { picker.launch("image/*") })
            }
        }
    }
}
