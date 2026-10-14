package com.miojo.cerebelo.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.Streaks
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.components.InkBackButton
import com.miojo.cerebelo.ui.components.InkCheckbox
import com.miojo.cerebelo.ui.components.InkFrame
import com.miojo.cerebelo.ui.components.InkFrameStyle
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.components.InkScreenTitle
import com.miojo.cerebelo.ui.components.InkSectionHeader
import com.miojo.cerebelo.ui.theme.Ink
import com.miojo.cerebelo.ui.theme.InkSizes
import com.miojo.cerebelo.ui.weekdayInitial
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private const val WEEKS = 12

/** Histórico de um afazer: números e uma grade das últimas semanas (tocar corrige um dia). */
@Composable
fun HabitDetailScreen(habitId: Long, onBack: () -> Unit) {
    val vm = appViewModel { HabitDetailViewModel(habitId, it.habitRepository) }
    val habit by vm.habit.collectAsStateWithLifecycle()
    val days by vm.days.collectAsStateWithLifecycle()
    val today = LocalDate.now()

    Column(
        Modifier
            .fillMaxSize()
            .background(Ink.colors.page)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Ink.spacing.screen),
    ) {
        InkBackButton(onClick = onBack, modifier = Modifier.padding(top = Ink.spacing.sm))
        InkScreenTitle(habit?.name ?: "", Modifier.padding(top = Ink.spacing.sm))
        InkLabel("Histórico", Modifier.padding(top = Ink.spacing.xs))
        val done = days ?: return@Column
        val last30 = (0L until 30L).count { (today.toEpochDay() - it) in done }

        Row(Modifier.padding(top = Ink.spacing.lg), horizontalArrangement = Arrangement.spacedBy(Ink.spacing.sm)) {
            Stat("Seguidos agora", Streaks.current(done, today), Modifier.weight(1f))
            Stat("Melhor sequência", Streaks.best(done), Modifier.weight(1f))
        }
        Row(Modifier.padding(top = Ink.spacing.sm), horizontalArrangement = Arrangement.spacedBy(Ink.spacing.sm)) {
            Stat("Últimos 30 dias", last30, Modifier.weight(1f))
            Stat("Total de dias", done.size, Modifier.weight(1f))
        }

        InkSectionHeader("Últimas $WEEKS semanas", Modifier.padding(top = Ink.spacing.xl, bottom = Ink.spacing.md))
        HistoryGrid(done = done, today = today, onToggle = vm::toggle)
        InkLabel(
            "Toque num dia para marcar ou desmarcar",
            Modifier.padding(top = Ink.spacing.md, bottom = Ink.spacing.xl).navigationBarsPadding(),
        )
    }
}

@Composable
private fun Stat(label: String, value: Int, modifier: Modifier = Modifier) {
    InkFrame(modifier, style = InkFrameStyle.Single, contentPadding = PaddingValues(14.dp)) {
        Column {
            Text("$value", style = Ink.type.bigNumber.copy(fontSize = Ink.type.section.fontSize * 1.6f), color = Ink.colors.seal)
            InkLabel(label, color = Ink.colors.ink)
        }
    }
}

/** Colunas = semanas, linhas = dias (seg a dom). */
@Composable
private fun HistoryGrid(done: Set<Long>, today: LocalDate, onToggle: (LocalDate) -> Unit) {
    val firstMonday = today
        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .minusWeeks((WEEKS - 1).toLong())

    Column(Modifier.fillMaxWidth()) {
        for (row in 0 until 7) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                InkLabel(weekdayInitial(firstMonday.plusDays(row.toLong())), Modifier.width(16.dp))
                for (week in 0 until WEEKS) {
                    val date = firstMonday.plusWeeks(week.toLong()).plusDays(row.toLong())
                    val future = date.isAfter(today)
                    Box(
                        Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .then(if (future) Modifier else Modifier.clickable(onClickLabel = date.toString()) { onToggle(date) }),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (!future) {
                            InkCheckbox(
                                checked = date.toEpochDay() in done,
                                onCheckedChange = null,
                                size = 20.dp,
                                alpha = if (date == today) 1f else if (date.toEpochDay() in done) 1f else InkSizes.MISSED_ALPHA,
                            )
                        }
                    }
                }
            }
        }
    }
}
