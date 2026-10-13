package com.miojo.cerebelo.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.miojo.cerebelo.data.TagRef
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.data.db.TagType
import com.miojo.cerebelo.ui.label
import com.miojo.cerebelo.ui.tagChipText
import com.miojo.cerebelo.ui.theme.spacing

/** Chip de tag somente leitura (usado nas notas). */
@Composable
fun TagChip(name: String, type: TagType, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(tagChipText(name, type)) },
        icon = if (type == TagType.PERSON) {
            { Icon(Icons.Default.Person, contentDescription = null, Modifier.size(SuggestionChipDefaults.IconSize)) }
        } else null,
        colors = if (type == TagType.PERSON) {
            SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        } else {
            SuggestionChipDefaults.suggestionChipColors()
        },
    )
}

/** Tags selecionadas (removíveis) + botões para adicionar pessoa/acontecimento. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagEditorRow(
    selected: List<TagRef>,
    allTags: List<TagEntity>,
    onAdd: (TagRef) -> Unit,
    onRemove: (TagRef) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickerType by rememberSaveable { mutableStateOf<TagType?>(null) }

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        selected.forEach { tag ->
            InputChip(
                selected = true,
                onClick = { onRemove(tag) },
                label = { Text(tagChipText(tag.name, tag.type)) },
                leadingIcon = if (tag.type == TagType.PERSON) {
                    { Icon(Icons.Default.Person, null, Modifier.size(InputChipDefaults.IconSize)) }
                } else null,
                trailingIcon = {
                    Icon(Icons.Default.Close, "Remover", Modifier.size(InputChipDefaults.IconSize))
                },
            )
        }
        TagType.entries.forEach { type ->
            AssistChip(
                onClick = { pickerType = type },
                label = { Text(type.label) },
                leadingIcon = { Icon(Icons.Default.Add, null, Modifier.size(AssistChipDefaults.IconSize)) },
            )
        }
    }

    pickerType?.let { type ->
        TagPickerDialog(
            title = "Adicionar ${type.label.lowercase()}",
            tags = allTags.filter { tag ->
                tag.type == type && selected.none { it.type == type && it.name.equals(tag.name, ignoreCase = true) }
            },
            createType = type,
            onPick = { onAdd(it); pickerType = null },
            onDismiss = { pickerType = null },
        )
    }
}

/**
 * Diálogo para escolher uma tag existente (com busca) ou criar uma nova.
 * Se [createType] for nulo, só permite escolher (usado no filtro).
 */
@Composable
fun TagPickerDialog(
    title: String,
    tags: List<TagEntity>,
    createType: TagType?,
    onPick: (TagRef) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val typed = text.trim()
    val matches = tags.filter { typed.isEmpty() || it.name.contains(typed, ignoreCase = true) }
    val exact = tags.firstOrNull { it.name.equals(typed, ignoreCase = true) }

    fun confirm() {
        when {
            exact != null -> onPick(TagRef.of(exact))
            typed.isNotEmpty() && createType != null -> onPick(TagRef(typed, createType))
            matches.size == 1 -> onPick(TagRef.of(matches.first()))
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    placeholder = { Text(if (createType != null) "Nome" else "Buscar") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { confirm() }),
                )
                LazyColumn(Modifier.heightIn(max = 240.dp)) {
                    if (createType != null && typed.isNotEmpty() && exact == null) {
                        item {
                            PickerRow("Criar “$typed”") { onPick(TagRef(typed, createType)) }
                        }
                    }
                    items(matches, key = { it.id }) { tag ->
                        PickerRow(tagChipText(tag.name, tag.type)) { onPick(TagRef.of(tag)) }
                    }
                }
                if (matches.isEmpty() && createType == null) {
                    Text("Nenhuma tag encontrada.", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (createType != null) {
                TextButton(onClick = ::confirm, enabled = typed.isNotEmpty()) { Text("Adicionar") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
private fun PickerRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = MaterialTheme.spacing.md, horizontal = MaterialTheme.spacing.xs),
    )
}
