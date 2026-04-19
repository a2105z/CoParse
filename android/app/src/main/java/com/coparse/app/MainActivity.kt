package com.coparse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.coparse.app.ui.navigation.CoparseNavHost
import com.coparse.app.ui.theme.CoParseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoParseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val mainVm: MainViewModel = viewModel()
                    CoparseNavHost(
                        navController = navController,
                        modifier = Modifier.fillMaxSize(),
                        mainVm = mainVm,
                    )
                }
            }
        }
    }
}
