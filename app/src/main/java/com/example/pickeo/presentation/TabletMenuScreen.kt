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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import com.example.pickeo.domain.models.CartLine
import com.example.pickeo.domain.models.MenuItem
import com.example.pickeo.domain.models.MenuSection
import com.example.pickeo.ui.theme.PickeoTheme

@Composable
fun TabletMenuScreen(
    viewModel: TabletMenuViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(16.dp)
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

            CartList(
                lines = state.cart,
                onDismiss = { viewModel.removeLine(it.item) }
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            Text(
                "Total: $${state.total}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.amountReceived,
                onValueChange = viewModel::updateAmountReceived,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cash Received") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors()
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Change: $${state.change}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.End),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
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
private fun MenuSectionGrid(
    section: MenuSection,
    onItemClick: (MenuItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 500.dp)
    ) {
        items(
            items = section.items,
            key = { it.id }
        ) { item ->
            MenuItemCard(item, onClick = { onItemClick(item) })
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
    modifier: Modifier = Modifier
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
                                val qtyText = if (line.quantity <= 1) "" else "x${line.quantity}"
                                Text(
                                    text = qtyText,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val lineTotal = line.item.price.multiply(BigDecimal(line.quantity))
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
    name = "Tablet 10\" 1280x800 - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 1280,
    heightDp = 800
)
@Composable
fun TabletMenuScreenPreview_Tablet_Dark() {
    PickeoTheme {
        TabletMenuScreen()
    }
}