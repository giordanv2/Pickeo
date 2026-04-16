package com.example.order_entry.presentation

import android.app.Activity
import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.example.cart_feat.presentation.CartRoute
import com.example.cart_feat.presentation.CartScreen
import com.example.cart_feat.presentation.CartUiState
import com.example.cart_lib.models.CartItem
import com.example.cart_lib.models.CartSummary
import com.example.catalog_feat.presentation.CatalogRoute
import com.example.catalog_feat.presentation.CatalogScreen
import com.example.catalog_feat.presentation.CatalogUiState
import com.example.catalog_lib.models.Catalog
import com.example.catalog_lib.models.CatalogItem
import com.example.catalog_lib.models.CatalogSection
import com.example.core.designsystem.theme.PickeoTheme
import com.example.core.ui.PreviewDark
import com.example.core.ui.PreviewDarkExpanded
import com.example.core.ui.PreviewDarkExpandedPortrait
import com.example.core.ui.PreviewDarkLandscape
import com.example.orders_feat.presentation.OrdersRoute
import com.example.orders_feat.presentation.OrdersScreen
import com.example.orders_feat.presentation.OrdersUiState
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun OrderEntryRoute() {
    OrderEntryScreen()
}

@Composable
fun OrderEntryScreen() {
    OrderEntryScreen(
        catalogContent = {
            CatalogRoute()
        },
        cartContent = {
            CartRoute()
        },
        ordersContent = {
            OrdersRoute()
        }
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun OrderEntryScreen(
    catalogContent: @Composable BoxScope.() -> Unit,
    cartContent: @Composable BoxScope.() -> Unit,
    ordersContent: @Composable BoxScope.() -> Unit,
) {
    val configuration = LocalConfiguration.current
    val windowSizeClass = rememberWindowSizeClass()
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedDestination by rememberSaveable { mutableStateOf(OrderEntryDestination.OrderEntry) }
    val showCartPane = !(
        windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact &&
            configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(240.dp)
            ) {
                Text(
                    text = "Navigate",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)
                )
                OrderEntryDestination.entries.forEach { destination ->
                    NavigationDrawerItem(
                        label = { Text(destination.label) },
                        selected = selectedDestination == destination,
                        onClick = {
                            selectedDestination = destination
                            scope.launch { drawerState.close() }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        modifier = Modifier.padding(
                            NavigationDrawerItemDefaults.ItemPadding
                        )
                    )
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedDestination) {
                OrderEntryDestination.OrderEntry -> {
                    if (!showCartPane) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            catalogContent()
                        }
                        return@Box
                    }

                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxHeight()
                        ) {
                            catalogContent()
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
                            cartContent()
                        }
                    }
                }

                OrderEntryDestination.Catalog -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        catalogContent()
                    }
                }

                OrderEntryDestination.Cart -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        cartContent()
                    }
                }

                OrderEntryDestination.Orders -> {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ordersContent()
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(32.dp),
            ) {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open navigation menu"
                    )
                }
            }
        }
    }
}

private enum class OrderEntryDestination(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    OrderEntry(
        label = "Order Entry",
        icon = Icons.Default.Home
    ),
    Catalog(
        label = "Catalog",
        icon = Icons.AutoMirrored.Filled.List
    ),
    Cart(
        label = "Cart",
        icon = Icons.Default.ShoppingCart
    ),
    Orders(
        label = "Orders",
        icon = Icons.AutoMirrored.Filled.List
    )
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun rememberWindowSizeClass(): WindowSizeClass {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    return (context as? Activity)?.let { activity ->
        calculateWindowSizeClass(activity)
    } ?: WindowSizeClass.calculateFromSize(
        DpSize(
            width = configuration.screenWidthDp.dp,
            height = configuration.screenHeightDp.dp
        )
    )
}

@PreviewDark
@PreviewDarkLandscape
@PreviewDarkExpanded
@PreviewDarkExpandedPortrait
@Composable
private fun OrderEntryScreenPreview() {
    val sampleCatalog = previewCatalog()
    PickeoTheme {
        OrderEntryScreen(
            catalogContent = {
                CatalogScreen(
                    state = CatalogUiState(
                        isLoading = false,
                        catalog = sampleCatalog,
                        selectedSectionId = null,
                        visibleItems = sampleCatalog.sections.flatMap { it.items },
                        editableItems = sampleCatalog.sections.flatMap { it.items }
                    ),
                    onEvent = {}
                )
            },
            cartContent = {
                CartScreen(
                    state = CartUiState(
                        isLoading = false,
                        summary = CartSummary(
                            items = listOf(
                                CartItem(
                                    productId = "espresso",
                                    name = "Espresso",
                                    unitPrice = "2.50".toBigDecimal(),
                                    quantity = 2
                                ),
                                CartItem(
                                    productId = "latte",
                                    name = "Caffe Latte",
                                    unitPrice = "4.25".toBigDecimal(),
                                    quantity = 1
                                )
                            )
                        )
                    ),
                    onEvent = {}
                )
            },
            ordersContent = {
                OrdersScreen(
                    state = OrdersUiState(
                        isLoading = false,
                        orders = emptyList(),
                        selectedOrderId = null
                    ),
                    onEvent = {}
                )
            }
        )
    }
}

private fun previewCatalog(): Catalog {
    val coffee = CatalogSection(
        id = "coffee",
        title = "Coffee",
        items = listOf(
            CatalogItem(
                id = "espresso",
                name = "Espresso",
                price = BigDecimal("2.50")
            ),
            CatalogItem(
                id = "americano",
                name = "Americano",
                price = BigDecimal("3.00")
            ),
            CatalogItem(
                id = "latte",
                name = "Caffe Latte",
                price = BigDecimal("4.25"),
                isAvailable = false
            )
        )
    )
    val food = CatalogSection(
        id = "food",
        title = "Bakery",
        items = listOf(
            CatalogItem(
                id = "croissant",
                name = "Butter Croissant",
                price = BigDecimal("3.75")
            ),
            CatalogItem(
                id = "muffin",
                name = "Blueberry Muffin",
                price = BigDecimal("3.50")
            )
        )
    )

    return Catalog(
        id = "preview-catalog",
        name = "Preview Menu",
        sections = listOf(coffee, food)
    )
}
