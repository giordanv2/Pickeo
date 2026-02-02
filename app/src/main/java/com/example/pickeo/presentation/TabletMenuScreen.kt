package com.example.pickeo.presentation

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pickeo.domain.models.CartLine
import com.example.pickeo.domain.models.MenuItem
import com.example.pickeo.domain.models.MenuSection
import com.example.pickeo.ui.theme.PickeoTheme
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.roundToInt


data class FlyItem(
    val token: Long,
    val item: MenuItem,
    val from: Offset
)
@Composable
fun TabletMenuScreen(
    viewModel: TabletMenuViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // overlay + fly-to-cart
    val flyingItems = remember { mutableStateListOf<FlyItem>() }
    var cartTarget by remember { mutableStateOf<Offset?>(null) }


    // cart UI
    val cartListState = rememberLazyListState()
    var editingLine by remember { mutableStateOf<CartLine?>(null) }

    // auto-scroll solo cuando se agrega
    var prevSize by remember { mutableIntStateOf(state.cart.size) }
    LaunchedEffect(state.cart.size) {
        val newSize = state.cart.size
        val added = newSize > prevSize
        prevSize = newSize
        if (added && newSize > 0) {
            cartListState.animateScrollToItem(newSize - 1)
        }
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            LeftMenu(
                sections = state.sections,
                modifier = Modifier.weight(1f),

                // 👇 ahora recibimos item + posición
                onItemClick = { item, fromOffset ->
                    // ✅ Agrega inmediato (ya no se “demora”)
                    viewModel.addItem(item)

                    // ✅ Dispara animación (si tenemos target)
                    val target = cartTarget
                    if (target != null) {
                        flyingItems.add(
                            FlyItem(
                                token = System.nanoTime(),
                                item = item,
                                from = fromOffset
                            )
                        )
                    }
                }
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

                // 👇 punto de aterrizaje (puedes moverlo si quieres)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .onGloballyPositioned { coords ->
                            val p = coords.localToRoot(Offset.Zero)
                            cartTarget = p + Offset(80f, 60f)
                        }
                )

                CartList(
                    lines = state.cart,
                    onDismiss = { viewModel.removeLine(it.item) },
                    onQuantityClick = { editingLine = it },
                    listState = cartListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                if (editingLine != null) {
                    QuantityDialog(
                        line = editingLine!!,
                        onDismiss = { editingLine = null },
                        onUpdate = { newQty ->
                            viewModel.updateQuantity(editingLine!!.item, newQty)
                            editingLine = null
                        }
                    )
                }

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
                        .width(150.dp)
                        .height(60.dp)
                        .align(Alignment.End),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = "Pay Now", fontSize = 20.sp)
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.clearCart() },
                    modifier = Modifier
                        .width(150.dp)
                        .height(60.dp)
                        .align(Alignment.End),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(text = "Clear", fontSize = 20.sp)
                }

                Spacer(Modifier.height(8.dp))
            }
        }

        // ✅ overlay animado (vuela y al final agrega)
        flyingItems.forEach { fly ->
            FlyToCartOverlay(
                fly = fly,
                to = cartTarget,
                onDone = { done -> flyingItems.remove(done) }
            )
        }

    }
}
//@Composable
//fun TabletMenuScreen(
//    viewModel: TabletMenuViewModel = viewModel()
//) {
//    val state by viewModel.uiState.collectAsStateWithLifecycle()
//
//    Row(
//        modifier = Modifier
//            .background(MaterialTheme.colorScheme.background)
//            .fillMaxSize()
//            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
//    ) {
//        LeftMenu(
//            sections = state.sections,
//            onItemClick = { viewModel.addItem(it) },
//            modifier = Modifier.weight(1f)
//        )
//
//        // RIGHT: Cart + pago
//        Column(
//            modifier = Modifier
//                .weight(1f)
//                .padding(start = 8.dp)
//        ) {
//            Text(
//                text = "Selected Items",
//                style = MaterialTheme.typography.titleLarge,
//                color = MaterialTheme.colorScheme.primary
//            )
//            Spacer(Modifier.height(8.dp))
//
//            val cartListState = rememberLazyListState()
//            var editingLine by remember { mutableStateOf<CartLine?>(null) }
//
//            CartList(
//                lines = state.cart,
//                onDismiss = { viewModel.removeLine(it.item) },
//                onQuantityClick = { editingLine = it },
//                listState = cartListState,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//            )
//
//            if (editingLine != null) {
//                QuantityDialog(
//                    line = editingLine!!,
//                    onDismiss = { editingLine = null },
//                    onUpdate = { newQty ->
//                        viewModel.updateQuantity(editingLine!!.item, newQty)
//                        editingLine = null
//                    }
//                )
//            }
//            var prevSize by remember { mutableIntStateOf(state.cart.size) }
//
//            LaunchedEffect(state.cart.size) {
//                val newSize = state.cart.size
//                val added = newSize > prevSize
//                prevSize = newSize
//
//                if (added && newSize > 0) {
//                    cartListState.animateScrollToItem(newSize - 1)
//                }
//            }
//
//
//
//
//
////            Spacer(modifier = Modifier.weight(1f))
//            HorizontalDivider()
//
//            Text(
//                "Total: $${state.total}",
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier
//                    .align(Alignment.End)
//                    .padding(top = 8.dp, end = 8.dp),
//                color = MaterialTheme.colorScheme.onBackground,
//                fontSize = 20.sp
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            Button(
//                onClick = { },
//                modifier = Modifier
//                    .width(150.dp)
//                    .height(60.dp)
//                    .align(Alignment.End),
//                shape = RoundedCornerShape(6.dp)
//            ) {
//                Text(
//                    text = "Pay Now",
//                    fontSize = 20.sp)
//            }
//
//            Spacer(Modifier.height(12.dp))
//
//            Button(
//                onClick = { viewModel.clearCart() },
//                modifier = Modifier
//                    .width(150.dp)
//                    .height(60.dp)
//                    .align(Alignment.End),
//                shape = RoundedCornerShape(6.dp)
//            ) {
//                Text(
//                    text = "Clear",
//                    fontSize = 20.sp)
//            }
//
//            Spacer(Modifier.height(8.dp))
//        }
//    }
//}

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
    onItemClick: (MenuItem, Offset) -> Unit,   // ✅ CAMBIO
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxHeight(),
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
                MenuItemCard(
                    item = item,
                    onClick = { offset -> onItemClick(item, offset) }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

//@Composable
//fun LeftMenu(
//    sections: List<MenuSection>,
//    onItemClick: (MenuItem) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    LazyVerticalGrid(
//        columns = GridCells.Fixed(4),
//        modifier = modifier.fillMaxHeight(), // no weight here
//        contentPadding = PaddingValues(12.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp),
//        horizontalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        sections.forEach { section ->
//            item(span = { GridItemSpan(maxLineSpan) }) {
//                Text(
//                    text = section.title,
//                    style = MaterialTheme.typography.titleLarge,
//                    color = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.padding(vertical = 4.dp)
//                )
//            }
//            items(items = section.items, key = { it.id }) { item ->
//                MenuItemCard(item) { onItemClick(item) }
//            }
//            item(span = { GridItemSpan(maxLineSpan) }) {
//                Spacer(Modifier.height(8.dp))
//            }
//        }
//    }
//}


@Composable
private fun MenuItemCard(
    item: MenuItem,
    onClick: (Offset) -> Unit
) {
    var pos by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .padding(1.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .onGloballyPositioned { coords ->
                pos = coords.localToRoot(Offset.Zero)
            }
            .clickable { onClick(pos) },
        elevation = cardElevation(defaultElevation = 4.dp),
        shape = androidx.compose.ui.graphics.RectangleShape
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


//@Composable
//private fun MenuItemCard(item: MenuItem, onClick: () -> Unit) {
//    Card(
//        modifier = Modifier
//            .padding(1.dp)
//            .fillMaxWidth()
//            .aspectRatio(1f)
//            .clickable { onClick() },
//        elevation = cardElevation(defaultElevation = 4.dp),
//        shape = RectangleShape
//    ) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                "${item.name}\n$${item.price}",
//                textAlign = TextAlign.Center,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartList(
    lines: List<CartLine>,
    onDismiss: (CartLine) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    onQuantityClick: (CartLine) -> Unit, // ← NUEVO
) {
    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
        ) {
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

                                QtyWave(
                                    qty = line.quantity,
                                    onClick = { onQuantityClick(line) },
                                    modifier = Modifier.weight(1f)
                                )

//                                Text(
//                                    text = "x$displayQty",
//                                    modifier = Modifier
//                                        .weight(1f)
//                                        .clickable { onQuantityClick(line) },
//                                    textAlign = TextAlign.Center,
//                                    color = MaterialTheme.colorScheme.primary
//                                )

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

@Composable
private fun FlyToCartOverlay(
    fly: FlyItem,
    to: Offset?,
    onDone: (FlyItem) -> Unit
) {
    if (to == null) return

    val key = fly.token
    val x = remember(key) { Animatable(fly.from.x) }
    val y = remember(key) { Animatable(fly.from.y) }
    val a = remember(key) { Animatable(1f) }
    val s = remember(key) { Animatable(1f) }

    LaunchedEffect(key) {
        // 🔥 más rápido: ~150ms
        x.animateTo(to.x, tween(140))
        y.animateTo(to.y, tween(140))
        s.animateTo(0.92f, tween(90))
        a.animateTo(0f, tween(90))
        onDone(fly)
    }

    Surface(
        modifier = Modifier
            .offset { IntOffset(x.value.roundToInt(), y.value.roundToInt()) }
            .scale(s.value)
            .alpha(a.value),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f)
    ) {
        Text(
            text = fly.item.name,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}



@Composable
fun QtyWave(
    qty: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    waveColor: Color = MaterialTheme.colorScheme.primary,
) {
    val displayQty = max(1, qty)

    val wave = remember { Animatable(0f) }

    // Para evitar animación en el primer render (cuando se agrega por primera vez)
    var hasRenderedOnce by remember { mutableStateOf(false) }
    var prevQty by remember { mutableIntStateOf(displayQty) }

    LaunchedEffect(displayQty) {
        val shouldAnimate = hasRenderedOnce && displayQty != prevQty
        prevQty = displayQty
        hasRenderedOnce = true

        if (shouldAnimate) {
            wave.snapTo(0f)
            wave.animateTo(1f, tween(620))
        } else {
            wave.snapTo(0f) // se queda invisible
        }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .drawBehind {
                val maxRadius = size.minDimension / 1.7f
                val r = maxRadius * wave.value
                val a = (1f - wave.value).coerceIn(0f, 1f) * 0.30f

                drawCircle(
                    color = waveColor.copy(alpha = a),
                    radius = r,
                    center = center
                )
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .widthIn(min = 44.dp),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = displayQty, label = "qtyCrossfade") { q ->
            Text(text = "x$q", color = MaterialTheme.colorScheme.primary)
        }
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