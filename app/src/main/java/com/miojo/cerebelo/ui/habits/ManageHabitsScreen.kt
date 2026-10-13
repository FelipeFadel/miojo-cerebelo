package com.miojo.cerebelo.ui.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.db.HabitEntity
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHabitsScreen(onBack: () -> Unit) {
    val vm = appViewModel { ManageHabitsViewModel(it.habitRepository) }
    val habits by vm.habits.collectAsStateWithLifecycle()
    var newName by rememberSaveable { mutableStateOf("") }
    var renaming by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleting by rememberSaveable { mutableStateOf<Long?>(null) }

    fun add() {
        if (newName.isNotBlank()) {
            vm.add(newName)
            newName = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar checklist") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            contentPadding = PaddingValues(MaterialTheme.spacing.screen),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            item(key = "add") {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Novo item (ex.: Exercício)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { add() }),
                    trailingIcon = {
                        IconButton(onClick = ::add, enabled = newName.isNotBlank()) {
                            Icon(Icons.Default.Add, "Adicionar")
                        }
                    },
                )
            }
            itemsIndexed(habits, key = { _, habit -> habit.id }) { index, habit ->
                ManageRow(
                    habit = habit,
                    canMoveUp = index > 0,
                    canMoveDown = index < habits.lastIndex,
                    onRename = { renaming = habit.id },
                    onMoveUp = { vm.moveUp(habit.id) },
                    onMoveDown = { vm.moveDown(habit.id) },
                    onDelete = { deleting = habit.id },
                )
            }
        }
    }

    habits.firstOrNull { it.id == renaming }?.let { habit ->
        RenameDialog(
            initial = habit.name,
            onConfirm = { vm.rename(habit.id, it); renaming = null },
            onDismiss = { renaming = null },
        )
    }

    habits.firstOrNull { it.id == deleting }?.let { habit ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Apagar “${habit.name}”?") },
            text = { Text("O histórico desse item também será apagado.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(habit.id); deleting = null }) { Text("Apagar") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun ManageRow(
    habit: HabitEntity,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onRename: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = habit.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onRename)
                    .padding(MaterialTheme.spacing.lg),
            )
            IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                Icon(Icons.Default.KeyboardArrowUp, "Subir")
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                Icon(Icons.Default.KeyboardArrowDown, "Descer")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Apagar")
            }
        }
    }
}

@Composable
private fun RenameDialog(initial: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renomear") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
