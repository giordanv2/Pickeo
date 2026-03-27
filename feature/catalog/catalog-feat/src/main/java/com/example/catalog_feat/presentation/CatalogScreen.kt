package com.example.catalog_feat.presentation

import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.designsystem.theme.PickeoTheme
import com.example.catalog_lib.models.Catalog
import com.example.catalog_lib.models.CatalogItem
import com.example.catalog_lib.models.CatalogSection
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState
import java.math.RoundingMode

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CatalogScreen(
        state = state,
        onEvent = { event ->
            viewModel.onEvent(event)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatalogScreen(
    state: CatalogUiState,
    onEvent: (CatalogUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val createCatalogItemDialogState = rememberCreateCatalogItemDialogState()
    val lazyGridState = rememberLazyGridState()
    val reorderableGridState = rememberReorderableLazyGridState(lazyGridState) { from, to ->
        onEvent(CatalogUiEvent.ReorderItemMoved(from.index, to.index))
    }
    val isDebugBuild = LocalContext.current.applicationContext.applicationInfo.flags and
        ApplicationInfo.FLAG_DEBUGGABLE != 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (isDebugBuild) {
                FloatingActionButton(onClick = { onEvent(CatalogUiEvent.CreateMockCatalogItemClicked) }) {
                    Text("Mock")
                }
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
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(CatalogUiEvent.SearchChanged(it)) },
                label = { Text("Search catalog") },
                singleLine = true,
                enabled = !state.isEditMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedSectionId == null,
                        onClick = {
                            if (!state.isEditMode) onEvent(CatalogUiEvent.SectionSelected(null))
                        },
                        label = { Text("All") }
                    )

                    state.sections.forEach { section ->
                        FilterChip(
                            selected = state.selectedSectionId == section.id,
                            onClick = {
                                if (!state.isEditMode) onEvent(CatalogUiEvent.SectionSelected(section.id))
                            },
                            label = { Text(section.title) }
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                if (state.isEditMode) {
                    IconButton(
                        onClick = { onEvent(CatalogUiEvent.UndoEditModeClicked) },
                        enabled = state.canUndoEditChange
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Undo last catalog edit")
                    }
                    IconButton(onClick = { onEvent(CatalogUiEvent.CancelEditModeClicked) }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel catalog reorder")
                    }
                    IconButton(onClick = { onEvent(CatalogUiEvent.ConfirmEditModeClicked) }) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm catalog reorder")
                    }
                } else {
                    IconButton(
                        onClick = { onEvent(CatalogUiEvent.EnterEditModeClicked) },
                        enabled = state.visibleItems.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit catalog order")
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 164.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp),
                state = lazyGridState,
                modifier = Modifier.fillMaxSize()
            ) {
                val displayedItems = if (state.isEditMode) state.editableItems else state.visibleItems
                items(displayedItems, key = { it.id }) { item ->
                    ReorderableItem(reorderableGridState, key = item.id) { isDragging ->
                        CatalogItemCard(
                            item = item,
                            isEditMode = state.isEditMode,
                            isDragging = isDragging,
                            modifier = if (state.isEditMode) {
                                Modifier.longPressDraggableHandle(
                                    onDragStarted = { onEvent(CatalogUiEvent.EditDragStarted) },
                                    onDragStopped = { onEvent(CatalogUiEvent.EditDragStopped) }
                                )
                            } else {
                                Modifier
                            },
                            onAddClicked = { onEvent(CatalogUiEvent.AddToCartClicked(item)) },
                            onDeleteClicked = {
                                onEvent(CatalogUiEvent.DeleteCatalogItemClicked(item.id))
                            }
                        )
                    }
                }
                if (!state.isEditMode) {
                    item(key = "create-catalog-item-card") {
                        CreateCatalogItemCard(
                            onClick = createCatalogItemDialogState::show
                        )
                    }
                }
            }
        }
    }

    CreateCatalogItemDialog(
        state = createCatalogItemDialogState,
        onCreateSubmitted = { name, price, sectionTitle ->
            onEvent(
                CatalogUiEvent.CreateCatalogItemSubmitted(
                    name = name,
                    price = price,
                    sectionTitle = sectionTitle
                )
            )
        }
    )
}

@Composable
private fun CatalogItemCard(
    item: CatalogItem,
    isEditMode: Boolean,
    isDragging: Boolean,
    modifier: Modifier,
    onAddClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    val alpha = if (item.isAvailable) 1f else 0.5f
    val wiggleDirection = if (item.id.hashCode() % 2 == 0) 1f else -1f
    val wiggleTransition = rememberInfiniteTransition(label = "catalog-wiggle")
    val wiggleRotation by wiggleTransition.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "catalog-wiggle-rotation"
    )
    val wiggleScale by wiggleTransition.animateFloat(
        initialValue = 0.992f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 170, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "catalog-wiggle-scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationZ = if (isEditMode) wiggleRotation * wiggleDirection else 0f
                scaleX = if (isEditMode) wiggleScale else 1f
                scaleY = if (isEditMode) wiggleScale else 1f
            }
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isDragging) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .then(if (isEditMode) modifier else Modifier)
                .clickable(enabled = item.isAvailable && !isEditMode, onClick = onAddClicked)
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
                    maxLines = 1,
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

        if (isEditMode) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(32.dp)
            ) {
                IconButton(
                    onClick = onDeleteClicked,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete catalog item")
                }
            }
        }
    }
}

@Composable
private fun CreateCatalogItemCard(onClick: () -> Unit) {
    val outlineColor = MaterialTheme.colorScheme.primaryContainer
    val shape = MaterialTheme.shapes.medium

    Card(
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(
                width = 1.dp,
                color = outlineColor,
                shape = shape
            )
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(168.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Create item",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    shape: Shape,
    dashOn: Dp = 16.dp,
    dashOff: Dp = 12.dp
): Modifier = drawWithCache {
    val strokeWidth = width.toPx()
    val inset = strokeWidth / 2f
    val dash = PathEffect.dashPathEffect(floatArrayOf(dashOn.toPx(), dashOff.toPx()))
    val outline = shape.createOutline(
        size = Size(size.width - strokeWidth, size.height - strokeWidth),
        layoutDirection = layoutDirection,
        density = this
    )

    onDrawWithContent {
        drawContent()
        translate(inset, inset) {
            drawOutline(
                outline = outline,
                color = color,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    pathEffect = dash
                )
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CatalogScreenPreview() {
    val sampleCatalog = previewCatalog()
    PickeoTheme {
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
    PickeoTheme() {
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
    }
}

@Preview(
    name = "Expanded",
    device = Devices.NEXUS_10,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    )
@Composable
private fun CatalogScreenPreview3() {
    val sampleCatalog = previewCatalog()
    PickeoTheme {
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
