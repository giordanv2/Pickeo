package com.example.pickeo.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.catalog_feat.presentation.CatalogRoute
import com.example.pickeo.ui.theme.PickeoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PickeoTheme {
                CatalogRoute()
            }
        }
    }
}
