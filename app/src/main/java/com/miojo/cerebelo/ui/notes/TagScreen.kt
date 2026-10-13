package com.miojo.cerebelo.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.data.db.TagType
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.formatDateTime
import com.miojo.cerebelo.ui.label
import com.miojo.cerebelo.ui.tagChipText
import com.miojo.cerebelo.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagScreen(
    tagId: Long,
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onTagClick: (TagEntity) -> Unit,
    onNewNote: (TagEntity) -> Unit,
) {
    val vm = appViewModel { TagViewModel(tagId, it.noteRepository) }
    val tag by vm.tag.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val deleted by vm.deleted.collectAsStateWithLifecycle()
    var showRename by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(deleted) { if (deleted) onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tag?.let { tagChipText(it.name, it.type) } ?: "")
                        tag?.let {
                            Text(
                                it.type.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                },
                actions = {
                    IconButton(onClick = { showRename = true }) { Icon(Icons.Default.Edit, "Renomear") }
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "Apagar tag") }
                },
            )
        },
        floatingActionButton = {
            tag?.let { current ->
                ExtendedFloatingActionButton(
                    onClick = { onNewNote(current) },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Nova nota") },
                )
            }
        },
    ) { padding ->
        val list = notes ?: return@Scaffold
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(MaterialTheme.spacing.screen),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            item(key = "summary") {
                Text(
                    text = when (list.size) {
                        0 -> "Nenhuma nota."
                        1 -> "1 nota"
                        else -> "${list.size} notas, desde ${formatDateTime(list.first().note.createdAt)}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(list, key = { it.note.id }) { item ->
                NoteCard(
                    item = item,
                    header = formatDateTime(item.note.createdAt),
                    onClick = { onOpenNote(item.note.id) },
                    onTagClick = { if (it.id != tagId) onTagClick(it) },
                )
            }
        }
    }

    val current = tag
    if (showRename && current != null) {
        RenameTagDialog(
            tag = current,
            showError = vm.renameError.collectAsStateWithLifecycle().value,
            onRename = { vm.rename(it) { showRename = false } },
            onDismiss = { showRename = false; vm.clearRenameError() },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Apagar tag?") },
            text = {
                Text(
                    if (current?.type == TagType.PERSON) "As notas continuam salvas, só perdem a marcação desta pessoa."
                    else "As notas continuam salvas, só perdem esta marcação."
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; vm.delete() }) { Text("Apagar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun RenameTagDialog(
    tag: TagEntity,
    showError: Boolean,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(tag.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renomear") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError,
                supportingText = if (showError) {
                    { Text("Já existe uma tag com esse nome.") }
                } else null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
        },
        confirmButton = {
            TextButton(onClick = { onRename(name) }, enabled = name.isNotBlank()) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
