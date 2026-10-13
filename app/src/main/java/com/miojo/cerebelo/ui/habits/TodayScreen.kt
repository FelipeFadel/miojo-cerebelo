package com.miojo.cerebelo.ui.habits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.HabitForDay
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.formatFullDay
import com.miojo.cerebelo.ui.theme.spacing

@Composable
fun TodayScreen(
    onOpenHabit: (Long) -> Unit,
    onManageHabits: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = appViewModel { HabitsViewModel(it.habitRepository) }
    val today by vm.today.collectAsStateWithLifecycle()
    val items by vm.items.collectAsStateWithLifecycle()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refreshDate() }

    val list = items ?: return
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.spacing.screen),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        item(key = "header") {
            Column(Modifier.padding(bottom = MaterialTheme.spacing.sm)) {
                Text(formatFullDay(today), style = MaterialTheme.typography.titleLarge)
                if (list.isNotEmpty()) {
                    val done = list.count { it.done }
                    Text(
                        "$done de ${list.size} feitos",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { done.toFloat() / list.size },
                        modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.sm),
                    )
                }
            }
        }

        if (list.isEmpty()) {
            item(key = "empty") {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                    Text(
                        "Nenhum item no checklist ainda. Adicione coisas que quer fazer todo dia, como exercício ou suplementos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = onManageHabits) { Text("Montar checklist") }
                }
            }
        }

        items(list, key = { it.habit.id }) { item ->
            HabitRow(
                item = item,
                onToggle = { vm.toggle(item.habit.id) },
                onOpen = { onOpenHabit(item.habit.id) },
            )
        }
    }
}

@Composable
private fun HabitRow(item: HabitForDay, onToggle: () -> Unit, onOpen: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.done) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = MaterialTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = item.done, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f)) {
                Text(item.habit.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = streakText(item.streak),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onOpen) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Histórico")
            }
        }
    }
}

fun streakText(streak: Int): String = when (streak) {
    0 -> "Sem sequência"
    1 -> "1 dia seguido"
    else -> "$streak dias seguidos"
}
