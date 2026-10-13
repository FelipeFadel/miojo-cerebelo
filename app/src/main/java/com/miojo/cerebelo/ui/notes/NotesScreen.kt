package com.miojo.cerebelo.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.TagRef
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.formatDay
import com.miojo.cerebelo.ui.formatTime
import com.miojo.cerebelo.ui.tagChipText
import com.miojo.cerebelo.ui.theme.spacing

/** Tela inicial: campo de nova nota + busca/filtro + linha do tempo por dia. */
@Composable
fun NotesScreen(
    focusRequest: Int,
    pendingTag: TagRef?,
    onPendingTagConsumed: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onTagClick: (TagEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = appViewModel { NotesViewModel(it.noteRepository) }

    LaunchedEffect(pendingTag) {
        pendingTag?.let {
            vm.addTag(it)
            onPendingTagConsumed()
        }
    }

    val composer by vm.composer.collectAsStateWithLifecycle()
    val tags by vm.tags.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val filterTag by vm.filterTag.collectAsStateWithLifecycle()
    val timeline by vm.timeline.collectAsStateWithLifecycle()
    var showFilterPicker by rememberSaveable { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val composerFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // Pedido de "nova nota" (abertura do app ou widget): rola ao topo e foca o campo.
    LaunchedEffect(focusRequest) {
        if (focusRequest <= vm.handledFocusRequest) return@LaunchedEffect
        vm.markFocusHandled(focusRequest)
        listState.scrollToItem(0)
        repeat(10) {
            // O campo pode ainda não estar desenhado; tenta nos próximos quadros.
            val focused = runCatching { composerFocus.requestFocus() }.isSuccess
            if (focused) {
                keyboard?.show()
                return@LaunchedEffect
            }
            withFrameNanos { }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(MaterialTheme.spacing.screen),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        item(key = "composer") {
            NoteComposer(
                state = composer,
                allTags = tags,
                focusRequester = composerFocus,
                onTextChange = vm::onTextChange,
                onAddTag = vm::addTag,
                onRemoveTag = vm::removeTag,
                onSave = vm::save,
            )
        }

        item(key = "search") {
            SearchBar(
                query = query,
                onQueryChange = vm::onQueryChange,
                filterTag = filterTag,
                onFilterClick = {
                    if (filterTag != null) vm.setFilter(null) else showFilterPicker = true
                },
                modifier = Modifier.padding(top = MaterialTheme.spacing.md),
            )
        }

        val groups = timeline.groups
        if (groups != null && groups.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = if (timeline.isFiltering) "Nada encontrado." else "Nenhuma nota ainda. Escreva a primeira acima.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.xl),
                )
            }
        }

        groups.orEmpty().forEach { group ->
            item(key = "day-${group.date.toEpochDay()}") {
                Text(
                    text = formatDay(group.date),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.md),
                )
            }
            items(group.notes, key = { it.note.id }) { item ->
                NoteCard(
                    item = item,
                    header = formatTime(item.note.createdAt),
                    onClick = { onOpenNote(item.note.id) },
                    onTagClick = onTagClick,
                )
            }
        }
    }

    if (showFilterPicker) {
        TagPickerDialog(
            title = "Filtrar por tag",
            tags = tags,
            createType = null,
            onPick = { picked ->
                vm.setFilter(picked.id)
                showFilterPicker = false
            },
            onDismiss = { showFilterPicker = false },
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    filterTag: TagEntity?,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Buscar") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                    }
                }
            } else null,
            singleLine = true,
        )
        FilterChip(
            selected = filterTag != null,
            onClick = onFilterClick,
            label = { Text(filterTag?.let { tagChipText(it.name, it.type) } ?: "Tag") },
            leadingIcon = {
                Icon(
                    imageVector = if (filterTag != null) Icons.Default.Close else Icons.AutoMirrored.Filled.List,
                    contentDescription = if (filterTag != null) "Remover filtro" else "Filtrar por tag",
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
        )
    }
}
