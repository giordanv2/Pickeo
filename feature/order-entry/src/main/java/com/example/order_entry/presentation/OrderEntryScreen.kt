package com.example.order_entry.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalog_feat.presentation.CatalogScreen
import com.example.cart_feat.presentation.CartRoute

@Composable
fun OrderEntryRoute(
    viewModel: OrderEntryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OrderEntryScreen(
        cartItemCount = state.cartSummary.totalItems,
        onCatalogItemAdded = viewModel::onCatalogItemAdded
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryScreen(
    cartItemCount: Int,
    onCatalogItemAdded: (String, String, java.math.BigDecimal) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Entry", style = MaterialTheme.typography.titleLarge) }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            ) {
                CatalogScreen(
                    cartItemCount = cartItemCount,
                    showTopBar = false,
                    showBottomBar = false,
                    onItemAdded = { },
                    onViewCartClicked = {}
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            ) {}

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                CartRoute(showTopBar = false)
            }
        }
    }
}
