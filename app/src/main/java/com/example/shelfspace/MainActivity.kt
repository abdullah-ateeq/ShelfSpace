package com.example.shelfspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.shelfspace.ui.navigation.ShelfSpaceNavGraph
import com.example.shelfspace.ui.theme.ShelfSpaceTheme
import com.example.shelfspace.viewmodel.ShelfSpaceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ShelfSpaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            ShelfSpaceTheme(
                themeMode = uiState.settings.themeMode,
                dynamicColor = uiState.settings.dynamicColor
            ) {
                ShelfSpaceNavGraph(viewModel = viewModel)
            }
        }
    }
}
