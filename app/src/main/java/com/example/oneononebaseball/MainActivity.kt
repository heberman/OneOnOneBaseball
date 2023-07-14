package com.example.oneononebaseball

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oneononebaseball.ui.BaseballApp
import com.example.oneononebaseball.ui.screens.BaseballViewModel
import com.example.oneononebaseball.ui.theme.OneOnOneBaseballTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OneOnOneBaseballTheme {
                val viewModel: BaseballViewModel =
                    viewModel(factory = BaseballViewModel.Factory)
                BaseballApp(viewModel)
            }
        }
    }
}