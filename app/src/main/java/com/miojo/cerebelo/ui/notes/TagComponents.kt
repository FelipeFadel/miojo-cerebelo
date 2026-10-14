package com.miojo.cerebelo.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.miojo.cerebelo.data.TagRef
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.data.db.TagType
import com.miojo.cerebelo.ui.components.InkDialog
import com.miojo.cerebelo.ui.components.InkFrameStyle
import com.miojo.cerebelo.ui.components.InkTag
import com.miojo.cerebelo.ui.components.InkTextButton
import com.miojo.cerebelo.ui.components.InkTextField
import com.miojo.cerebelo.ui.label
import com.miojo.cerebelo.ui.theme.Ink

/** Tags escolhidas (tocar remove) + botões para adicionar pessoa/acontecimento. */
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
        horizontalArrangement = Arrangement.spacedBy(Ink.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Ink.spacing.xs),
    ) {
        selected.forEach { tag ->
            InkTag(tag.name, tag.type, trailing = "×", onClick = { onRemove(tag) })
        }
        InkTextButton("+ Pessoa", onClick = { pickerType = TagType.PERSON }, frame = InkFrameStyle.Person, color = Ink.colors.person)
        InkTextButton(
            "+ Acontecimento",
            onClick = { pickerType = TagType.EVENT },
            frame = InkFrameStyle.Event,
            color = Ink.colors.seal,
            italic = true,
        )
    }

    pickerType?.let { type ->
        TagPickerDialog(
            title = if (type == TagType.PERSON) "Quem?" else "Qual acontecimento?",
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
 * Escolher uma tag existente (com busca) ou criar uma nova.
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
    val canCreate = createType != null && typed.isNotEmpty() && exact == null

    fun confirm() {
        when {
            exact != null -> onPick(TagRef.of(exact))
            canCreate -> onPick(TagRef(typed, createType!!))
            matches.size == 1 -> onPick(TagRef.of(matches.first()))
        }
    }

    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    InkDialog(
        title = title,
        onDismiss = onDismiss,
        confirmLabel = if (createType != null) "Adicionar" else null,
        onConfirm = ::confirm,
        confirmEnabled = typed.isNotEmpty(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Ink.spacing.sm)) {
            InkTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = createType?.label?.let { "Nome" } ?: "Buscar",
                modifier = Modifier.focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { confirm() }),
            )
            LazyColumn(Modifier.heightIn(max = 260.dp)) {
                if (canCreate) {
                    item { PickerRow("Criar “$typed”", createType!!, isCreate = true) { onPick(TagRef(typed, createType)) } }
                }
                items(matches, key = { it.id }) { tag ->
                    PickerRow(tag.name, tag.type) { onPick(TagRef.of(tag)) }
                }
            }
            if (matches.isEmpty() && !canCreate) {
                Text("Nada encontrado.", style = Ink.type.item, color = Ink.colors.inkSoft)
            }
        }
    }
}

@Composable
private fun PickerRow(text: String, type: TagType, isCreate: Boolean = false, onClick: () -> Unit) {
    val isEvent = type == TagType.EVENT
    Text(
        text = text,
        style = Ink.type.item.copy(fontStyle = if (isEvent || isCreate) FontStyle.Italic else FontStyle.Normal),
        color = when {
            isCreate -> Ink.colors.inkSoft
            isEvent -> Ink.colors.seal
            else -> Ink.colors.ink
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
    )
}
