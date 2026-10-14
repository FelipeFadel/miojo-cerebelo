package com.miojo.cerebelo.ui.habits

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.HabitForDay
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.components.InkCheckbox
import com.miojo.cerebelo.ui.components.InkItemText
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.components.InkRule
import com.miojo.cerebelo.ui.components.InkScreenTitle
import com.miojo.cerebelo.ui.components.InkSectionHeader
import com.miojo.cerebelo.ui.components.InkTextButton
import com.miojo.cerebelo.ui.components.OrnamentOfDay
import com.miojo.cerebelo.ui.formatLongDay
import com.miojo.cerebelo.ui.theme.Ink

/** "Afazeres": sequência da semana e checklist de hoje. Segurar um item abre o histórico. */
@Composable
fun TodayScreen(
    onOpenHabit: (Long) -> Unit,
    onEditList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = appViewModel { HabitsViewModel(it.habitRepository) }
    val today by vm.today.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refreshDate() }

    val list = items
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Ink.spacing.screen, end = Ink.spacing.screen, bottom = Ink.spacing.xl),
    ) {
        item(key = "header") {
            Column(Modifier.statusBarsPadding().padding(top = Ink.spacing.md), horizontalAlignment = Alignment.CenterHorizontally) {
                OrnamentOfDay()
                InkScreenTitle("Afazeres", Modifier.padding(top = Ink.spacing.sm), textAlign = TextAlign.Center)
                val count = if (!list.isNullOrEmpty()) " · ${list.count { it.done }} de ${list.size}" else ""
                InkLabel(formatLongDay(today) + count, Modifier.padding(top = Ink.spacing.xs), textAlign = TextAlign.Center)
                val currentProgress = progress
                if (currentProgress != null && !list.isNullOrEmpty()) {
                    StreakWeek(currentProgress, today, Modifier.padding(top = Ink.spacing.lg))
                }
            }
        }

        item(key = "today-header") {
            InkSectionHeader(
                "Hoje",
                modifier = Modifier.padding(top = Ink.spacing.lg),
                trailing = { InkTextButton("Editar lista", onClick = onEditList, color = Ink.colors.inkSoft) },
            )
        }

        if (list != null && list.isEmpty()) {
            item(key = "empty") {
                Text(
                    "Nenhum afazer ainda. Toque em EDITAR LISTA para criar os seus (ex.: exercício, vitaminas).",
                    style = Ink.type.item,
                    color = Ink.colors.inkSoft,
                    modifier = Modifier.padding(vertical = Ink.spacing.md),
                )
            }
        }

        items(list.orEmpty(), key = { it.habit.id }) { item ->
            TodoListRow(item, onToggle = { vm.toggle(item.habit.id) }, onLongPress = { onOpenHabit(item.habit.id) })
        }

        if (!list.isNullOrEmpty()) {
            item(key = "hint") {
                InkLabel(
                    "Segure um item para ver o histórico",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = Ink.spacing.lg),
                )
            }
        }
    }
}

/** Item com checkbox de tinta e divisória embaixo. Tocar marca; segurar abre o histórico. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodoListRow(item: HabitForDay, onToggle: () -> Unit, onLongPress: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .semantics {
                role = Role.Checkbox
                stateDescription = if (item.done) "Feito" else "Não feito"
            }
            .combinedClickable(onClick = onToggle, onLongClick = onLongPress, onLongClickLabel = "Ver histórico"),
    ) {
        Row(Modifier.padding(vertical = Ink.spacing.md), verticalAlignment = Alignment.CenterVertically) {
            InkCheckbox(checked = item.done, onCheckedChange = null)
            Spacer(Modifier.width(Ink.spacing.md))
            InkItemText(item.habit.name, item.done)
        }
        InkRule()
    }
}
