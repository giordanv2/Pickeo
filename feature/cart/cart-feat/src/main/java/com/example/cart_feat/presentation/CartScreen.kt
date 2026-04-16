package com.example.cart_feat.presentation

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cart_lib.models.CartItem
import com.example.cart_lib.models.CartSummary
import com.example.core.designsystem.theme.PickeoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CartRoute(
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CartScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    state: CartUiState,
    onEvent: (CartUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemShape = MaterialTheme.shapes.medium
    val isCartEmpty = state.summary.items.isEmpty()
    var revealedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var revealedItemAction by rememberSaveable { mutableStateOf<RevealedSwipeAction?>(null) }
    var notesByItemId by rememberSaveable { mutableStateOf<Map<String, String>>(emptyMap()) }
    var editingNoteItemId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingNoteText by rememberSaveable { mutableStateOf("") }
    val dismissRevealInteractionSource = remember { MutableInteractionSource() }

    fun closeRevealedItemActions() {
        revealedItemId = null
        revealedItemAction = null
    }

    LaunchedEffect(state.summary.items, revealedItemId, revealedItemAction, notesByItemId, editingNoteItemId) {
        val validItemIds = state.summary.items.map { it.productId }.toSet()
        if (revealedItemId != null && state.summary.items.none { it.productId == revealedItemId }) {
            closeRevealedItemActions()
        }
        if (notesByItemId.keys.any { it !in validItemIds }) {
            notesByItemId = notesByItemId.filterKeys { it in validItemIds }
        }
        if (editingNoteItemId != null && editingNoteItemId !in validItemIds) {
            editingNoteItemId = null
            editingNoteText = ""
        }
    }

    val closeRevealModifier = if (revealedItemId != null) {
        Modifier.clickable(
            interactionSource = dismissRevealInteractionSource,
            indication = null
        ) {
            closeRevealedItemActions()
        }
    } else {
        Modifier
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Cart")
                },
                actions = {
                    IconButton(
                        onClick = {
                            closeRevealedItemActions()
                            onEvent(CartUiEvent.ClearClicked)
                        },
                        enabled = !isCartEmpty
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Clear whole cart"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading cart...")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.then(closeRevealModifier),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isCartEmpty) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Cart is empty")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.summary.items, key = { it.productId }) { item ->
                            CartItemCard(
                                item = item,
                                shape = itemShape,
                                revealedAction = if (revealedItemId == item.productId) {
                                    revealedItemAction
                                } else {
                                    null
                                },
                                onRevealedActionChanged = { revealedAction ->
                                    when {
                                        revealedAction != null -> {
                                            revealedItemId = item.productId
                                            revealedItemAction = revealedAction
                                        }
                                        revealedItemId == item.productId -> closeRevealedItemActions()
                                    }
                                },
                                onDelete = {
                                    if (revealedItemId == item.productId) {
                                        closeRevealedItemActions()
                                    }
                                    notesByItemId = notesByItemId - item.productId
                                    onEvent(CartUiEvent.RemoveClicked(item.productId))
                                },
                                onNote = {
                                    if (revealedItemId == item.productId) {
                                        closeRevealedItemActions()
                                    }
                                    editingNoteItemId = item.productId
                                    editingNoteText = notesByItemId[item.productId].orEmpty()
                                },
                                onIncrement = {
                                    if (revealedItemId == item.productId) {
                                        closeRevealedItemActions()
                                    }
                                    onEvent(CartUiEvent.IncrementClicked(item.productId))
                                },
                                onDecrement = {
                                    if (revealedItemId == item.productId) {
                                        closeRevealedItemActions()
                                    }
                                    onEvent(CartUiEvent.DecrementClicked(item.productId))
                                },
                                note = notesByItemId[item.productId].orEmpty()
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Items",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${state.summary.totalItems}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtotal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                formatUsd(state.summary.subtotal),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    closeRevealedItemActions()
                                    onEvent(CartUiEvent.SaveClicked(notesByItemId))
                                },
                                enabled = !isCartEmpty
                            ) {
                                Text("Save")
                            }
                            Button(
                                onClick = { closeRevealedItemActions() },
                                enabled = !isCartEmpty
                            ) {
                                Text("Checkout")
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingNoteItemId != null) {
        AlertDialog(
            onDismissRequest = {
                editingNoteItemId = null
                editingNoteText = ""
            },
            title = {
                Text("Item note")
            },
            text = {
                OutlinedTextField(
                    value = editingNoteText,
                    onValueChange = { editingNoteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Write a note...") },
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val itemId = editingNoteItemId ?: return@TextButton
                        val noteText = editingNoteText.trim()
                        notesByItemId = if (noteText.isEmpty()) {
                            notesByItemId - itemId
                        } else {
                            notesByItemId + (itemId to noteText)
                        }
                        editingNoteItemId = null
                        editingNoteText = ""
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        editingNoteItemId = null
                        editingNoteText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    shape: Shape,
    revealedAction: RevealedSwipeAction?,
    onRevealedActionChanged: (RevealedSwipeAction?) -> Unit,
    onDelete: () -> Unit,
    onNote: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    note: String
) {
    val noteActionWidth = 56.dp
    val deleteActionWidth = 56.dp
    val actionUnderlap = 10.dp
    val swipeThresholdFraction = 0.42f
    val density = androidx.compose.ui.platform.LocalDensity.current
    val noteRevealDistance = noteActionWidth + actionUnderlap
    val deleteRevealDistance = deleteActionWidth + actionUnderlap
    val noteOpenOffsetX = with(density) { -noteRevealDistance.toPx() }
    val deleteOpenOffsetX = with(density) { deleteRevealDistance.toPx() }
    val maxRevealDistancePx = maxOf(-noteOpenOffsetX, deleteOpenOffsetX)
    val swipeThresholdPx = maxRevealDistancePx * swipeThresholdFraction
    val switchThresholdPx = maxRevealDistancePx * 0.75f
    val velocityThresholdPx = with(density) { 120.dp.toPx() }
    val animationDurationMs = 220
    val coroutineScope = rememberCoroutineScope()
    var contentOffsetX by remember(item.productId) { mutableStateOf(0f) }

    val draggableState = rememberDraggableState { delta ->
        contentOffsetX = (contentOffsetX + delta).coerceIn(noteOpenOffsetX, deleteOpenOffsetX)
    }

    LaunchedEffect(revealedAction) {
        contentOffsetX = when (revealedAction) {
            RevealedSwipeAction.Notes -> noteOpenOffsetX
            RevealedSwipeAction.Delete -> deleteOpenOffsetX
            null -> 0f
        }
    }

    var previousQuantity by remember(item.productId) { mutableIntStateOf(item.quantity) }
    var highlightQuantity by remember(item.productId) { mutableStateOf(false) }

    LaunchedEffect(item.quantity) {
        if (item.quantity > previousQuantity) {
            highlightQuantity = true
            delay(420)
            highlightQuantity = false
        }
        previousQuantity = item.quantity
    }

    val quantityTextColor by animateColorAsState(
        targetValue = if (highlightQuantity) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 280),
        label = "quantityTextColor"
    )
    val lineTotalText = formatUsd(item.lineTotal)
    val priceSlotWidth = when {
        lineTotalText.length >= 13 -> 150.dp // e.g. $1,000,000.00
        lineTotalText.length >= 11 -> 128.dp // e.g. $100,000.00
        else -> 100.dp
    }
    val noteActionShape = remember(shape) {
        if (shape is CornerBasedShape) {
            shape.copy(
                topStart = CornerSize(0.dp),
                bottomStart = CornerSize(0.dp)
            )
        } else {
            shape
        }
    }
    val deleteActionShape = remember(shape) {
        if (shape is CornerBasedShape) {
            shape.copy(
                topEnd = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp)
            )
        } else {
            shape
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .zIndex(0f)
                .fillMaxHeight()
                .width(deleteRevealDistance),
            shape = deleteActionShape,
            colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = actionUnderlap),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete cart item",
                        tint = Color.White
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .zIndex(1f)
                .fillMaxHeight()
                .width(noteRevealDistance),
            shape = noteActionShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = actionUnderlap),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNote,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = "Add note to cart item",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .zIndex(2f)
                .offset { IntOffset(x = contentOffsetX.roundToInt(), y = 0) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = { velocity ->
                        coroutineScope.launch {
                            suspend fun animateOffsetTo(target: Float) {
                                animate(
                                    initialValue = contentOffsetX,
                                    targetValue = target,
                                    animationSpec = tween(durationMillis = animationDurationMs)
                                ) { value, _ ->
                                    contentOffsetX = value
                                }
                            }

                            val targetAction = when {
                                revealedAction == RevealedSwipeAction.Notes -> when {
                                    contentOffsetX <= -swipeThresholdPx -> RevealedSwipeAction.Notes
                                    contentOffsetX >= switchThresholdPx -> RevealedSwipeAction.Delete
                                    else -> null
                                }
                                revealedAction == RevealedSwipeAction.Delete -> when {
                                    contentOffsetX >= swipeThresholdPx -> RevealedSwipeAction.Delete
                                    contentOffsetX <= -switchThresholdPx -> RevealedSwipeAction.Notes
                                    else -> null
                                }
                                contentOffsetX <= -swipeThresholdPx || velocity < -velocityThresholdPx -> RevealedSwipeAction.Notes
                                contentOffsetX >= swipeThresholdPx || velocity > velocityThresholdPx -> RevealedSwipeAction.Delete
                                else -> null
                            }
                            val targetOffset = when (targetAction) {
                                RevealedSwipeAction.Notes -> noteOpenOffsetX
                                RevealedSwipeAction.Delete -> deleteOpenOffsetX
                                null -> 0f
                            }
                            animateOffsetTo(targetOffset)
                            onRevealedActionChanged(targetAction)
                        }
                    }
                ),
            shape = shape,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Product image placeholder",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.name,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (note.isNotBlank()) {
                        Text(
                            text = note,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.height(34.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = onDecrement,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(
                                        text = "-",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )

                                Text(
                                    text = "${item.quantity}",
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .widthIn(min = 36.dp)
                                        .padding(horizontal = 10.dp),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = quantityTextColor,
                                    textAlign = TextAlign.Center
                                )

                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .fillMaxHeight()
                                        .background(MaterialTheme.colorScheme.outlineVariant)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(topEnd = 999.dp, bottomEnd = 999.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                ) {
                                    IconButton(
                                        onClick = onIncrement,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Add,
                                            contentDescription = "Increase quantity",
                                            tint = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .width(priceSlotWidth)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Text(
                        text = lineTotalText,
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private enum class RevealedSwipeAction {
    Notes,
    Delete
}

private fun formatUsd(amount: java.math.BigDecimal): String {
    val formatter = DecimalFormat(
        "#,##0.00",
        DecimalFormatSymbols(Locale.US)
    )
    return "$${formatter.format(amount.setScale(2, RoundingMode.HALF_UP))}"
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    )
@Composable
private fun CartScreenPreview() {
    PickeoTheme() {
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
private fun CartScreenExpandedPreview() {
    PickeoTheme() {
        CartScreenPreview()
    }
}
