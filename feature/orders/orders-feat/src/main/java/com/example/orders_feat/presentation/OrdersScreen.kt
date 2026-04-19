package com.example.orders_feat.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.theme.PickeoTheme
import com.example.core.ui.PreviewDark
import com.example.core.ui.PreviewDarkExpanded
import com.example.core.ui.PreviewDarkExpandedPortrait
import com.example.orders_lib.models.Order
import java.math.BigDecimal

@Composable
fun OrdersRoute(
    onOrderLoaded: () -> Unit = {},
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(viewModel, onOrderLoaded) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                OrdersNavigationEvent.OpenCart -> onOrderLoaded()
            }
        }
    }
    OrdersScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun OrdersScreen(
    state: OrdersUiState,
    onEvent: (OrdersUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedOrder = state.orders.firstOrNull { it.id == state.selectedOrderId } ?: state.orders.firstOrNull()

    if (state.isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        val isWideLayout = maxWidth >= 840.dp

        if (isWideLayout) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OrdersGridPane(
                    orders = state.orders,
                    selectedOrderId = selectedOrder?.id,
                    onOrderSelected = { onEvent(OrdersUiEvent.OrderSelected(it)) },
                    modifier = Modifier.weight(2f)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}

                OrderDetailsPane(
                    order = selectedOrder,
                    isLoadingOrderIntoCart = state.isLoadingOrderIntoCart,
                    onLoadOrderClicked = { onEvent(OrdersUiEvent.LoadSelectedOrderClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OrdersListPane(
                    orders = state.orders,
                    selectedOrderId = selectedOrder?.id,
                    onOrderSelected = { onEvent(OrdersUiEvent.OrderSelected(it)) },
                    modifier = Modifier.weight(1f)
                )

                OrderDetailsPane(
                    order = selectedOrder,
                    isLoadingOrderIntoCart = state.isLoadingOrderIntoCart,
                    onLoadOrderClicked = { onEvent(OrdersUiEvent.LoadSelectedOrderClicked) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OrdersListPane(
    orders: List<Order>,
    selectedOrderId: String?,
    onOrderSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        OrdersHeader(modifier = Modifier.padding(start = 64.dp),orderCount = orders.size)
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                OrderSummaryCard(
                    order = order,
                    isSelected = order.id == selectedOrderId,
                    onClick = { onOrderSelected(order.id) }
                )
            }
        }
    }
}

@Composable
private fun OrdersGridPane(
    orders: List<Order>,
    selectedOrderId: String?,
    onOrderSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        Spacer(Modifier.height(16.dp))
        OrdersHeader(modifier = Modifier.padding(start = 72.dp), orderCount = orders.size)
        Spacer(Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 220.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                OrderSummaryCard(
                    order = order,
                    isSelected = order.id == selectedOrderId,
                    onClick = { onOrderSelected(order.id) }
                )
            }
        }
    }
}

@Composable
private fun OrdersHeader(modifier: Modifier, orderCount: Int) {
    Text(
        modifier = modifier,
        text = "Orders",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold
    )
    Text(
        modifier = modifier,
        text = "$orderCount created orders",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun OrderSummaryCard(
    order: Order,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.orderNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatCurrency(order.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = order.createdAtLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = order.customerName,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${order.items.sumOf { it.quantity }} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = order.status,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun OrderDetailsPane(
    order: Order?,
    isLoadingOrderIntoCart: Boolean,
    onLoadOrderClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        if (order == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Select an order to see details",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Card
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Order Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                DetailRow(label = "Order #", value = order.orderNumber)
                DetailRow(label = "Created", value = order.createdAtLabel)
                DetailRow(label = "Customer", value = order.customerName)
                DetailRow(label = "Status", value = order.status)
                DetailRow(label = "Total", value = formatCurrency(order.total))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Items",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.quantity}x ${item.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = formatCurrency(item.total),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (order.notes.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = order.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = onLoadOrderClicked,
                enabled = !isLoadingOrderIntoCart,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Text(if (isLoadingOrderIntoCart) "Loading..." else "Load Order")
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatCurrency(amount: BigDecimal): String = "$${amount.setScale(2)}"

@PreviewDark
@PreviewDarkExpanded
@PreviewDarkExpandedPortrait
@Composable
private fun OrdersScreenPreview() {
    PickeoTheme {
        OrdersScreen(
            state = OrdersUiState(
                isLoading = false,
                orders = previewOrders(),
                selectedOrderId = "ord-1002"
            ),
            onEvent = {}
        )
    }
}

private fun previewOrders(): List<Order> = listOf(
    Order(
        id = "ord-1001",
        orderNumber = "#1001",
        createdAtLabel = "8:42 AM",
        total = BigDecimal("16.25"),
        status = "Ready",
        customerName = "Avery Johnson",
        notes = "Extra hot latte and pack pastry separately.",
        items = listOf(
            com.example.orders_lib.models.OrderItem("1", "Espresso", 2, BigDecimal("5.00")),
            com.example.orders_lib.models.OrderItem("2", "Butter Croissant", 1, BigDecimal("3.75")),
            com.example.orders_lib.models.OrderItem("3", "Caffe Latte", 1, BigDecimal("7.50"))
        )
    ),
    Order(
        id = "ord-1002",
        orderNumber = "#1002",
        createdAtLabel = "9:05 AM",
        total = BigDecimal("11.50"),
        status = "In Progress",
        customerName = "Morgan Lee",
        notes = "Customer will pick up at front counter.",
        items = listOf(
            com.example.orders_lib.models.OrderItem("4", "Americano", 1, BigDecimal("3.00")),
            com.example.orders_lib.models.OrderItem("5", "Blueberry Muffin", 1, BigDecimal("3.50")),
            com.example.orders_lib.models.OrderItem("6", "Caffe Latte", 1, BigDecimal("5.00"))
        )
    )
)
