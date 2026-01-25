package com.example.pickeo.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.math.BigDecimal
import com.example.pickeo.domain.models.CartLine
import com.example.pickeo.domain.models.MenuItem
import com.example.pickeo.domain.models.MenuSection
import com.example.pickeo.ui.theme.PickeoTheme
import kotlin.math.max

@Composable
fun TabletMenuScreen(
    viewModel: TabletMenuViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        LeftMenu(
            sections = state.sections,
            onItemClick = { viewModel.addItem(it) },
            modifier = Modifier.weight(1f)
        )

        // RIGHT: Cart + pago
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = "Selected Items",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))

            var editingLine by remember { mutableStateOf<CartLine?>(null) }

            CartList(
                lines = state.cart,
                onDismiss = { viewModel.removeLine(it.item) },
                onQuantityClick = { editingLine = it }
            )

            if (editingLine != null) {
                QuantityDialog(
                    line = editingLine!!,
                    onDismiss = { editingLine = null },
                    onUpdate = { newQty ->
                        viewModel.updateQuantity(editingLine!!.item, newQty) // asegúrate de tener este métod
                        editingLine = null
                    }
                )
            }


            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider()

            Text(
                "Total: $${state.total}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp, end = 8.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .width(150.dp) // ajusta el número a tu gusto
                    .height(60.dp)
                    .align(Alignment.End),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Pay Now",
                    fontSize = 20.sp)
            }

            Spacer(Modifier.height(8.dp))

//            Text(
//                "Change: $${state.change}",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.align(Alignment.End),
//                color = MaterialTheme.colorScheme.onBackground
//            )
        }
    }
}

@Composable
fun TabletMenuContent(
    state: TabletMenuUiState,
    onAddItem: (MenuItem) -> Unit,
    onRemoveLine: (CartLine) -> Unit,
    onUpdateQty: (MenuItem, Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        LeftMenu(
            sections = state.sections,
            onItemClick = onAddItem,
            modifier = Modifier.weight(1f)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = "Selected Items",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            var editingLine by remember { mutableStateOf<CartLine?>(null) }

            CartList(
                lines = state.cart,
                onDismiss = onRemoveLine,
                onQuantityClick = { editingLine = it }
            )

            if (editingLine != null) {
                QuantityDialog(
                    line = editingLine!!,
                    onDismiss = { editingLine = null },
                    onUpdate = { qty ->
                        onUpdateQty(editingLine!!.item, qty)
                        editingLine = null
                    }
                )
            }

            Spacer(Modifier.weight(1f))
            HorizontalDivider()

            Text(
                "Total: $${state.total}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp, end = 8.dp),
                fontSize = 20.sp
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { /* later */ },
                modifier = Modifier
                    .width(150.dp)
                    .height(60.dp)
                    .align(Alignment.End)
            ) {
                Text("Pay Now", fontSize = 20.sp)
            }
        }
    }
}

private fun previewState() = TabletMenuUiState(
    sections = listOf(
        MenuSection(
            title = "Drinks",
            items = listOf(
                MenuItem(id = "1", name = "Coke", price = BigDecimal("2.50")),
                MenuItem(id = "2", name = "Sprite", price = BigDecimal("2.50")),
                MenuItem(id = "3", name = "Water", price = BigDecimal("1.50")),
            )
        ),
        MenuSection(
            title = "Food",
            items = listOf(
                MenuItem(id = "4", name = "Burger", price = BigDecimal("8.99")),
                MenuItem(id = "5", name = "Fries", price = BigDecimal("3.99")),
            )
        )
    ),
    cart = listOf(
        CartLine(MenuItem("1", "Coke", BigDecimal("2.50")), 2),
        CartLine(MenuItem("4", "Burger", BigDecimal("8.99")), 1),
    ),
)


@Composable
fun QuantityDialog(
    line: CartLine,
    onDismiss: () -> Unit,
    onUpdate: (Int) -> Unit
) {
    var qty by remember(line) { mutableStateOf(max(1, line.quantity)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .widthIn(min = 260.dp, max = 340.dp)
            .wrapContentHeight(),
        title = { Text(line.item.name, style = MaterialTheme.typography.titleMedium) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { if (qty > 1) qty-- }) {
                    Text("−", style = MaterialTheme.typography.titleLarge)
                }
                Text(
                    text = qty.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(56.dp)
                )
                IconButton(onClick = { qty++ }) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onUpdate(qty) }) { Text("Actualizar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun LeftMenu(
    sections: List<MenuSection>,
    onItemClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxHeight(), // no weight here
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sections.forEach { section ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(items = section.items, key = { it.id }) { item ->
                MenuItemCard(item) { onItemClick(item) }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(1.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = cardElevation(defaultElevation = 4.dp),
        shape = RectangleShape
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${item.name}\n$${item.price}",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartList(
    lines: List<CartLine>,
    onDismiss: (CartLine) -> Unit,
    modifier: Modifier = Modifier,
    onQuantityClick: (CartLine) -> Unit, // ← NUEVO
) {
    Box(modifier = modifier) {
        LazyColumn {
            items(
                count = lines.size,
                key = { index -> lines[index].item.id }
            ) { index ->
                val line = lines[index]
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart ||
                            value == SwipeToDismissBoxValue.StartToEnd
                        ) {
                            onDismiss(line)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val isStart = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
                        val alignment = if (isStart) Alignment.CenterStart else Alignment.CenterEnd
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(horizontal = 20.dp),
                            contentAlignment = alignment
                        ) {
                            Text(
                                text = "Deleting...",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    content = {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = line.item.name,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                // (quita tu qtyText actual y el Text que lo usaba)
// NUEVO: siempre mostrar al menos x1 y hacerlo clickable
                                val displayQty = max(1, line.quantity)

                                Text(
                                    text = "x$displayQty",
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onQuantityClick(line) }, // ← abre el diálogo
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )

// usa displayQty para el total
                                val lineTotal = line.item.price.multiply(BigDecimal(displayQty))
                                Text(
                                    text = "$$lineTotal",
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                )
            }
        }
    }
}



/* ---------- Previews ---------- */

@Preview(
    name = "Compact",
    widthDp = 411,
    heightDp = 891,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewCompact() {
    PickeoTheme {
        TabletMenuContent(
            state = previewState(),
            onAddItem = {},
            onRemoveLine = {},
            onUpdateQty = { _, _ -> }
        )
    }
}

@Preview(
    name = "Medium",
    widthDp = 600,
    heightDp = 891,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PreviewMedium() {
    PickeoTheme {
        TabletMenuContent(
            state = previewState(),
            onAddItem = {},
            onRemoveLine = {},
            onUpdateQty = { _, _ -> }
        )
    }
}

@Preview(
    name = "Expanded",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun PreviewExpanded() {
    PickeoTheme {
        TabletMenuScreen()
    }
}