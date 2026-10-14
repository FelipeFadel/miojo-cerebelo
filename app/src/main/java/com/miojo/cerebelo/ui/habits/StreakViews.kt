package com.miojo.cerebelo.ui.habits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.miojo.cerebelo.data.ChecklistProgress
import com.miojo.cerebelo.ui.components.DayMark
import com.miojo.cerebelo.ui.components.InkDayBox
import com.miojo.cerebelo.ui.components.InkFrame
import com.miojo.cerebelo.ui.components.InkFrameStyle
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.daysInRow
import com.miojo.cerebelo.ui.theme.Ink
import com.miojo.cerebelo.ui.weekdayInitial
import com.miojo.cerebelo.ui.weekdayShort
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Como mostrar um dia na faixa da sequência. */
fun dayMark(date: LocalDate, today: LocalDate, fulfilled: Set<Long>): DayMark = when {
    date.isAfter(today) -> DayMark.FUTURE
    date.toEpochDay() in fulfilled -> DayMark.DONE
    date == today -> DayMark.TODAY
    else -> DayMark.MISSED
}

/** Tela Notas: número grande + "DIAS SEGUIDOS" + os últimos 7 dias terminando hoje. */
@Composable
fun StreakLastSevenDays(progress: ChecklistProgress, today: LocalDate, modifier: Modifier = Modifier) {
    InkFrame(
        modifier = modifier.fillMaxWidth(),
        style = InkFrameStyle.Single,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${progress.streak}", style = Ink.type.bigNumber, color = Ink.colors.seal)
            Spacer(Modifier.width(Ink.spacing.sm))
            InkLabel("Dias\nseguidos", color = Ink.colors.ink)
            Spacer(Modifier.width(Ink.spacing.sm))
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceBetween) {
                for (back in 6 downTo 0) {
                    val date = today.minusDays(back.toLong())
                    InkDayBox(
                        label = weekdayInitial(date),
                        mark = dayMark(date, today, progress.fulfilledDays),
                        isToday = date == today,
                        boxSize = 22.dp,
                    )
                }
            }
        }
    }
}

/** Tela Afazeres: "Sequência" + "N dias seguidos" e a semana atual (seg a dom). */
@Composable
fun StreakWeek(progress: ChecklistProgress, today: LocalDate, modifier: Modifier = Modifier) {
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    InkFrame(modifier = modifier.fillMaxWidth(), style = InkFrameStyle.Double) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sequência", style = Ink.type.section, color = Ink.colors.ink, modifier = Modifier.weight(1f))
                Text(
                    daysInRow(progress.streak),
                    style = Ink.type.item.copy(fontStyle = FontStyle.Italic),
                    color = Ink.colors.inkSoft,
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(top = Ink.spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                for (offset in 0..6) {
                    val date = monday.plusDays(offset.toLong())
                    InkDayBox(
                        label = weekdayShort(date),
                        mark = dayMark(date, today, progress.fulfilledDays),
                        isToday = date == today,
                        boxSize = 26.dp,
                    )
                }
            }
        }
    }
}
