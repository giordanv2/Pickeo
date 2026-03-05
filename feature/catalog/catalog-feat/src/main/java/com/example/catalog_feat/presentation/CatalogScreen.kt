package com.example.catalog_feat.presentation

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalog_lib.models.Catalog
import com.example.catalog_lib.models.CatalogItem
import com.example.catalog_lib.models.CatalogSection
import java.math.RoundingMode

@Composable
fun CatalogRoute(
    viewModel: CatalogViewModel = hiltViewModel(),
    cartItemCount: Int = 0,
    showTopBar: Boolean = true,
    showBottomBar: Boolean = true,
    onItemAdded: (CatalogItem) -> Unit = {},
    onViewCartClicked: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogScreen(
        state = state,
        cartItemCount = cartItemCount,
        showTopBar = showTopBar,
        showBottomBar = showBottomBar,
        onViewCartClicked = onViewCartClicked,
        onEvent = { event ->
            viewModel.onEvent(event)
            if (event is CatalogUiEvent.AddToCartClicked && event.item.isAvailable) {
                onItemAdded(event.item)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    state: CatalogUiState,
    cartItemCount: Int,
    showTopBar: Boolean,
    showBottomBar: Boolean,
    onViewCartClicked: () -> Unit,
    onEvent: (CatalogUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var nameInput by rememberSaveable { mutableStateOf("") }
    var priceInput by rememberSaveable { mutableStateOf("") }
    var sectionInput by rememberSaveable { mutableStateOf("") }
    var dialogError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(text = state.catalog?.name ?: "Catalog")
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Items in cart: $cartItemCount")
                    OutlinedButton(onClick = onViewCartClicked) {
                        Text("View Cart")
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                dialogError = null
                showCreateDialog = true
            }) {
                Text("+")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = { onEvent(CatalogUiEvent.RetryClicked) }) {
                        Text(text = "Retry")
                    }
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(CatalogUiEvent.SearchChanged(it)) },
                label = { Text("Search catalog") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedSectionId == null,
                    onClick = { onEvent(CatalogUiEvent.SectionSelected(null)) },
                    label = { Text("All") }
                )
                state.sections.forEach { section ->
                    FilterChip(
                        selected = state.selectedSectionId == section.id,
                        onClick = { onEvent(CatalogUiEvent.SectionSelected(section.id)) },
                        label = { Text(section.title) }
                    )
                }
            }

            if (state.visibleItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 164.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.visibleItems, key = { it.id }) { item ->
                        CatalogItemCard(
                            item = item,
                            onAddClicked = { onEvent(CatalogUiEvent.AddToCartClicked(item)) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Catalog Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name*") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Price*") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sectionInput,
                        onValueChange = { sectionInput = it },
                        label = { Text("Section*") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    dialogError?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsedPrice = priceInput.trim().toBigDecimalOrNull()
                        if (nameInput.isBlank() || sectionInput.isBlank() || parsedPrice == null || parsedPrice <= java.math.BigDecimal.ZERO) {
                            dialogError = "Fill all required fields with a valid price."
                            return@TextButton
                        }

                        onEvent(
                            CatalogUiEvent.CreateCatalogItemSubmitted(
                                name = nameInput,
                                price = priceInput,
                                sectionTitle = sectionInput
                            )
                        )
                        nameInput = ""
                        priceInput = ""
                        sectionInput = ""
                        dialogError = null
                        showCreateDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CatalogItemCard(
    item: CatalogItem,
    onAddClicked: () -> Unit
) {
    val alpha = if (item.isAvailable) 1f else 0.5f
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = item.isAvailable, onClick = onAddClicked)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.name.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$${item.price.setScale(2, RoundingMode.HALF_UP)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            if (!item.isAvailable) {
                Text(
                    text = "Out of stock",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFB3261E)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogScreenPreview() {
    val sampleCatalog = previewCatalog()
    MaterialTheme {
        CatalogScreen(
            state = CatalogUiState(
                isLoading = false,
                catalog = sampleCatalog,
                selectedSectionId = null,
                visibleItems = sampleCatalog.sections.flatMap { it.items }
            ),
            cartItemCount = 3,
            showTopBar = true,
            showBottomBar = true,
            onViewCartClicked = {},
            onEvent = {}
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
private fun CatalogScreenPreview2() {
    val sampleCatalog = previewCatalog()
    MaterialTheme {
        CatalogScreen(
            state = CatalogUiState(
                isLoading = false,
                catalog = sampleCatalog,
                selectedSectionId = null,
                visibleItems = sampleCatalog.sections.flatMap { it.items }
            ),
            cartItemCount = 3,
            showTopBar = true,
            showBottomBar = true,
            onViewCartClicked = {},
            onEvent = {}
        )
    }
}

private fun previewCatalog(): Catalog {
    val items = listOf(
        CatalogItem(id = "espresso", name = "Espresso", price = "2.50".toBigDecimal()),
        CatalogItem(id = "latte", name = "Caffe Latte", price = "4.25".toBigDecimal()),
        CatalogItem(id = "croissant", name = "Croissant", price = "3.50".toBigDecimal())
    )
    return Catalog(
        id = "preview",
        name = "Catalog",
        sections = listOf(
            CatalogSection(id = "coffee", title = "Coffee", items = items.take(2)),
            CatalogSection(id = "bakery", title = "Bakery", items = items.takeLast(1))
        )
    )
}
