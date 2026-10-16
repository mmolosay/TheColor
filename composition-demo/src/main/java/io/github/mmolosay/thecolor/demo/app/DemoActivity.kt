package io.github.mmolosay.thecolor.demo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

@AndroidEntryPoint
class DemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TheColorTheme {
                // the recipe screen's View goes here once it's built
            }
        }
    }
}
