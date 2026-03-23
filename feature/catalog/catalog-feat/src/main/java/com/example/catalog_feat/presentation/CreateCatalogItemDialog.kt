package com.example.catalog_feat.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.math.BigDecimal

@Composable
internal fun rememberCreateCatalogItemDialogState(): CreateCatalogItemDialogState {
    var isVisible by rememberSaveable { mutableStateOf(false) }
    var nameInput by rememberSaveable { mutableStateOf("") }
    var priceInput by rememberSaveable { mutableStateOf("") }
    var sectionInput by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    return remember(isVisible, nameInput, priceInput, sectionInput, errorMessage) {
        CreateCatalogItemDialogState(
            isVisible = isVisible,
            nameInput = nameInput,
            priceInput = priceInput,
            sectionInput = sectionInput,
            errorMessage = errorMessage,
            onShow = {
                errorMessage = null
                isVisible = true
            },
            onDismiss = { isVisible = false },
            onNameChanged = { nameInput = it },
            onPriceChanged = { priceInput = it },
            onSectionChanged = { sectionInput = it },
            onSubmit = { onCreateSubmitted ->
                val parsedPrice = priceInput.trim().toBigDecimalOrNull()
                if (
                    nameInput.isBlank() ||
                    sectionInput.isBlank() ||
                    parsedPrice == null ||
                    parsedPrice <= BigDecimal.ZERO
                ) {
                    errorMessage = "Fill all required fields with a valid price."
                } else {
                    onCreateSubmitted(nameInput, priceInput, sectionInput)
                    nameInput = ""
                    priceInput = ""
                    sectionInput = ""
                    errorMessage = null
                    isVisible = false
                }
            }
        )
    }
}

internal class CreateCatalogItemDialogState(
    val isVisible: Boolean,
    val nameInput: String,
    val priceInput: String,
    val sectionInput: String,
    val errorMessage: String?,
    private val onShow: () -> Unit,
    private val onDismiss: () -> Unit,
    private val onNameChanged: (String) -> Unit,
    private val onPriceChanged: (String) -> Unit,
    private val onSectionChanged: (String) -> Unit,
    private val onSubmit: ((name: String, price: String, sectionTitle: String) -> Unit) -> Unit
) {
    fun show() = onShow()

    fun dismiss() = onDismiss()

    fun updateName(value: String) = onNameChanged(value)

    fun updatePrice(value: String) = onPriceChanged(value)

    fun updateSection(value: String) = onSectionChanged(value)

    fun submit(onCreateSubmitted: (name: String, price: String, sectionTitle: String) -> Unit) =
        onSubmit(onCreateSubmitted)
}

@Composable
internal fun CreateCatalogItemDialog(
    state: CreateCatalogItemDialogState,
    onCreateSubmitted: (name: String, price: String, sectionTitle: String) -> Unit
) {
    if (!state.isVisible) return

    AlertDialog(
        onDismissRequest = state::dismiss,
        title = { Text("Create Catalog Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = state::updateName,
                    label = { Text("Name*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.priceInput,
                    onValueChange = state::updatePrice,
                    label = { Text("Price*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.sectionInput,
                    onValueChange = state::updateSection,
                    label = { Text("Section*") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                state.errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { state.submit(onCreateSubmitted) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = state::dismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun CreateCatalogItemDialogPreview() {
    MaterialTheme {
        CreateCatalogItemDialog(
            state = CreateCatalogItemDialogState(
                isVisible = true,
                nameInput = "Latte",
                priceInput = "4.25",
                sectionInput = "Coffee",
                errorMessage = "Fill all required fields with a valid price.",
                onShow = {},
                onDismiss = {},
                onNameChanged = {},
                onPriceChanged = {},
                onSectionChanged = {},
                onSubmit = {}
            ),
            onCreateSubmitted = { _, _, _ -> }
        )
    }
}
