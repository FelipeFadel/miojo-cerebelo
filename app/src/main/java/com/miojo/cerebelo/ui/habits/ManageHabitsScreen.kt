package com.miojo.cerebelo.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.db.HabitEntity
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.components.InkBackButton
import com.miojo.cerebelo.ui.components.InkDialog
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.components.InkRule
import com.miojo.cerebelo.ui.components.InkScreenTitle
import com.miojo.cerebelo.ui.components.InkTextButton
import com.miojo.cerebelo.ui.components.InkTextField
import com.miojo.cerebelo.ui.theme.Ink
import com.miojo.cerebelo.ui.theme.InkSizes

/** Editar a lista de afazeres: adicionar, renomear (tocar no nome), reordenar e apagar. */
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink.colors.page)
            .statusBarsPadding()
            .imePadding(),
        contentPadding = PaddingValues(start = Ink.spacing.screen, end = Ink.spacing.screen, bottom = Ink.spacing.xl),
    ) {
        item(key = "header") {
            Column {
                InkBackButton(onClick = onBack, modifier = Modifier.padding(top = Ink.spacing.sm))
                InkScreenTitle("Editar lista", Modifier.padding(top = Ink.spacing.sm))
                InkLabel("Afazeres que reiniciam todo dia", Modifier.padding(top = Ink.spacing.xs, bottom = Ink.spacing.lg))
                InkTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    placeholder = "Novo afazer (ex.: Exercício)",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { add() }),
                    trailing = { InkTextButton("Adicionar", onClick = ::add, enabled = newName.isNotBlank()) },
                )
            }
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

        if (habits.isNotEmpty()) {
            item(key = "hint") {
                InkLabel("Toque no nome para renomear", Modifier.padding(top = Ink.spacing.md).navigationBarsPadding())
            }
        }
    }

    habits.firstOrNull { it.id == renaming }?.let { habit ->
        var name by rememberSaveable(habit.id) { mutableStateOf(habit.name) }
        InkDialog(
            title = "Renomear",
            onDismiss = { renaming = null },
            confirmLabel = "Guardar",
            onConfirm = { vm.rename(habit.id, name); renaming = null },
            confirmEnabled = name.isNotBlank(),
        ) {
            InkTextField(value = name, onValueChange = { name = it })
        }
    }

    habits.firstOrNull { it.id == deleting }?.let { habit ->
        InkDialog(
            title = "Apagar “${habit.name}”?",
            onDismiss = { deleting = null },
            confirmLabel = "Apagar",
            onConfirm = { vm.delete(habit.id); deleting = null },
        ) {
            Text("O histórico desse afazer também será apagado.", style = Ink.type.item, color = Ink.colors.inkSoft)
        }
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
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = habit.name,
                style = Ink.type.item,
                color = Ink.colors.ink,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClickLabel = "Renomear", onClick = onRename)
                    .padding(vertical = Ink.spacing.md),
            )
            InkIconButton(Icons.Default.KeyboardArrowUp, "Subir", enabled = canMoveUp, onClick = onMoveUp)
            InkIconButton(Icons.Default.KeyboardArrowDown, "Descer", enabled = canMoveDown, onClick = onMoveDown)
            InkTextButton("Apagar", onClick = onDelete, color = Ink.colors.seal)
        }
        InkRule()
    }
}

@Composable
private fun InkIconButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .alpha(if (enabled) 1f else InkSizes.MISSED_ALPHA)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = Ink.colors.ink, modifier = Modifier.size(24.dp))
    }
}
