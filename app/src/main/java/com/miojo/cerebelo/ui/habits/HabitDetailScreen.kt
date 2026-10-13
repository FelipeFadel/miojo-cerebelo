package com.miojo.cerebelo.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.Streaks
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.theme.spacing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private const val WEEKS = 15
private val WEEKDAY_LABELS = listOf("S", "T", "Q", "Q", "S", "S", "D")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(habitId: Long, onBack: () -> Unit) {
    val vm = appViewModel { HabitDetailViewModel(habitId, it.habitRepository) }
    val habit by vm.habit.collectAsStateWithLifecycle()
    val days by vm.days.collectAsStateWithLifecycle()
    val today = LocalDate.now()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
                },
            )
        },
    ) { padding ->
        val done = days ?: return@Scaffold
        val last30 = (0L until 30L).count { (today.toEpochDay() - it) in done }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(MaterialTheme.spacing.screen),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                StatCard("Sequência atual", Streaks.current(done, today), Modifier.weight(1f))
                StatCard("Melhor sequência", Streaks.best(done), Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                StatCard("Últimos 30 dias", last30, Modifier.weight(1f))
                StatCard("Total de dias", done.size, Modifier.weight(1f))
            }

            Text("Últimas $WEEKS semanas", style = MaterialTheme.typography.titleMedium)
            HistoryGrid(done = done, today = today, onToggle = vm::toggle)
            Text(
                "Toque num dia para marcar ou desmarcar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(MaterialTheme.spacing.md)) {
            Text("$value", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Grade estilo calendário: colunas = semanas, linhas = dias da semana (seg a dom). */
@Composable
private fun HistoryGrid(done: Set<Long>, today: LocalDate, onToggle: (LocalDate) -> Unit) {
    val firstMonday = today
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .minusWeeks((WEEKS - 1).toLong())

    Column(Modifier.fillMaxWidth()) {
        for (row in 0 until 7) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    WEEKDAY_LABELS[row],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(16.dp),
                )
                for (week in 0 until WEEKS) {
                    val date = firstMonday.plusWeeks(week.toLong()).plusDays(row.toLong())
                    DayCell(
                        date = date,
                        isDone = date.toEpochDay() in done,
                        isToday = date == today,
                        isFuture = date.isAfter(today),
                        onClick = { onToggle(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isDone: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(modifier.aspectRatio(1f).padding(1.5.dp)) {
        if (isFuture) return@Box
        var cell = Modifier
            .fillMaxSize()
            .clip(shape)
            .background(if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
        if (isToday) cell = cell.border(1.5.dp, MaterialTheme.colorScheme.onSurface, shape)
        Box(cell.clickable(onClickLabel = date.toString(), onClick = onClick))
    }
}
