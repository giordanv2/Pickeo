package com.example.pickeo.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.catalog_feat.presentation.CatalogRoute
import com.example.cart_feat.presentation.CartRoute
import com.example.cart_lib.models.CartSummary
import com.example.cart_lib.usecase.AddItemToCartUseCase
import com.example.cart_lib.usecase.ObserveCartUseCase
import com.example.pickeo.ui.theme.PickeoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var addItemToCartUseCase: AddItemToCartUseCase

    @Inject
    lateinit var observeCartUseCase: ObserveCartUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PickeoTheme {
                PickeoApp(
                    observeCartUseCase = observeCartUseCase,
                    addItemToCartUseCase = addItemToCartUseCase
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickeoApp(
    observeCartUseCase: ObserveCartUseCase,
    addItemToCartUseCase: AddItemToCartUseCase
) {
    val cartSummary by observeCartUseCase().collectAsState(initial = CartSummary())
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Pickeo POS", style = MaterialTheme.typography.titleLarge) })
        }
    ) { padding ->
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Catalog") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Cart (${cartSummary.totalItems})") }
                )
            }

            when (selectedTab) {
                0 -> CatalogRoute(
                    cartItemCount = cartSummary.totalItems,
                    onItemAdded = { item ->
                        scope.launch {
                            addItemToCartUseCase(
                                productId = item.id,
                                name = item.name,
                                unitPrice = item.price
                            )
                        }
                    },
                    onViewCartClicked = { selectedTab = 1 }
                )
                1 -> CartRoute()
            }
        }
    }
}
