package com.miojo.cerebelo.ui.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.HabitForDay
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.components.InkCheckbox
import com.miojo.cerebelo.ui.components.InkItemText
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.components.InkPlusButton
import com.miojo.cerebelo.ui.components.InkScreenTitle
import com.miojo.cerebelo.ui.components.InkSectionHeader
import com.miojo.cerebelo.ui.components.InkTextButton
import com.miojo.cerebelo.ui.formatLongDay
import com.miojo.cerebelo.ui.formatNoteWhen
import com.miojo.cerebelo.ui.habits.StreakLastSevenDays
import com.miojo.cerebelo.ui.theme.Ink
import com.miojo.cerebelo.ui.theme.InkAssets
import com.miojo.cerebelo.ui.theme.InkSizes

/** Tela inicial "Notas": dragão, sequência, últimas notas e afazeres de hoje. */
@Composable
fun NotesHomeScreen(
    onNewNote: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onTagClick: (TagEntity) -> Unit,
    onSeeAllNotes: () -> Unit,
    onEditTodos: () -> Unit,
    onBackup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = appViewModel { NotesHomeViewModel(it.noteRepository, it.habitRepository) }
    val today by vm.today.collectAsStateWithLifecycle()
    val recent by vm.recent.collectAsStateWithLifecycle()
    val todos by vm.todos.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refreshDate() }

    Box(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = Ink.spacing.screen),
            ) {
                Spacer(Modifier.height(Ink.spacing.dragonClearance))
                InkScreenTitle("Notas")
                InkLabel(formatLongDay(today), Modifier.padding(top = Ink.spacing.xs))

                val currentProgress = progress
                if (currentProgress != null && todos.isNotEmpty()) {
                    StreakLastSevenDays(currentProgress, today, Modifier.padding(top = Ink.spacing.lg))
                }

                val notes = recent
                Column(
                    Modifier.padding(top = Ink.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Ink.spacing.md),
                ) {
                    if (notes != null && notes.isEmpty()) {
                        Text(
                            "Nenhuma nota ainda. Toque no + para escrever a primeira.",
                            style = Ink.type.item,
                            color = Ink.colors.inkSoft,
                        )
                    }
                    notes.orEmpty().forEach { item ->
                        NoteCard(
                            item = item,
                            whenLabel = formatNoteWhen(item.note.createdAt, today),
                            onClick = { onOpenNote(item.note.id) },
                            onTagClick = onTagClick,
                            ledger = true,
                        )
                    }
                    if (notes != null && notes.isNotEmpty()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            InkTextButton("Ver todas", onClick = onSeeAllNotes, color = Ink.colors.inkSoft)
                        }
                    }
                }

                InkSectionHeader("Afazeres", Modifier.padding(top = Ink.spacing.md))
                if (todos.isEmpty()) {
                    Row(Modifier.padding(top = Ink.spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        Text("Nenhum afazer ainda.", style = Ink.type.item, color = Ink.colors.inkSoft, modifier = Modifier.weight(1f))
                        InkTextButton("Editar lista", onClick = onEditTodos)
                    }
                } else {
                    Column(Modifier.padding(top = Ink.spacing.sm)) {
                        todos.forEach { item -> TodoRow(item, onToggle = { vm.toggle(item.habit.id) }) }
                    }
                }

                Row(Modifier.fillMaxWidth().padding(top = Ink.spacing.xl), horizontalArrangement = Arrangement.Center) {
                    InkTextButton("Backup", onClick = onBackup, color = Ink.colors.inkSoft)
                }
                // Espaço para o botão + não cobrir o fim da página.
                Spacer(Modifier.height(InkSizes.plusButton + Ink.spacing.xl))
            }

            // Dragão por cima do conteúdo, rolando junto; não recebe toques.
            Image(
                painter = painterResource(InkAssets.dragon),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .width(InkSizes.dragonWidth)
                    .offset(x = InkSizes.dragonOffsetX, y = InkSizes.dragonOffsetY),
            )
        }

        InkPlusButton(
            onClick = onNewNote,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Ink.spacing.screen, bottom = Ink.spacing.md),
        )
    }
}

/** Afazer na tela Notas: tocar na linha marca/desmarca. */
@Composable
fun TodoRow(item: HabitForDay, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = item.done, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(vertical = Ink.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InkCheckbox(checked = item.done, onCheckedChange = null)
        Spacer(Modifier.width(Ink.spacing.md))
        InkItemText(item.habit.name, item.done)
    }
}
