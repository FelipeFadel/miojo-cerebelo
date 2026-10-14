package com.miojo.cerebelo.ui.people

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.data.db.TagType
import com.miojo.cerebelo.data.db.TagWithCount
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.components.InkFrame
import com.miojo.cerebelo.ui.components.InkFrameStyle
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.components.InkRule
import com.miojo.cerebelo.ui.components.InkScreenTitle
import com.miojo.cerebelo.ui.components.InkSectionHeader
import com.miojo.cerebelo.ui.components.InkTextButton
import com.miojo.cerebelo.ui.components.OrnamentOfDay
import com.miojo.cerebelo.ui.formatMonth
import com.miojo.cerebelo.ui.formatShortDay
import com.miojo.cerebelo.ui.notesCount
import com.miojo.cerebelo.ui.theme.Ink
import com.miojo.cerebelo.ui.theme.InkSizes
import androidx.compose.ui.draw.alpha
import java.time.YearMonth

/** "Pessoas": quem aparece nas notas (e acontecimentos), de todas as datas ou de um mês. */
@Composable
fun PeopleScreen(onOpenTag: (TagEntity) -> Unit, modifier: Modifier = Modifier) {
    val vm = appViewModel { PeopleViewModel(it.noteRepository) }
    val state by vm.state.collectAsStateWithLifecycle()
    val month by vm.month.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Ink.spacing.screen, end = Ink.spacing.screen, bottom = Ink.spacing.xl),
    ) {
        item(key = "header") {
            Column(Modifier.statusBarsPadding().padding(top = Ink.spacing.md)) {
                OrnamentOfDay()
                InkScreenTitle("Pessoas", Modifier.fillMaxWidth().padding(top = Ink.spacing.sm), textAlign = TextAlign.Center)
                PeriodPicker(
                    month = month,
                    onAll = vm::showAllDates,
                    onMonth = { vm.showMonth(month ?: YearMonth.now()) },
                    onPrevious = vm::previousMonth,
                    onNext = vm::nextMonth,
                    modifier = Modifier.padding(top = Ink.spacing.lg, bottom = Ink.spacing.sm),
                )
            }
        }

        val people = state.people
        val events = state.events
        if (people != null && events != null && people.isEmpty() && events.isEmpty()) {
            item(key = "empty") {
                Text(
                    if (month == null) "Ninguém marcado ainda. Marque pessoas nas notas."
                    else "Nenhuma nota em ${formatMonth(month!!).lowercase()}.",
                    style = Ink.type.item,
                    color = Ink.colors.inkSoft,
                    modifier = Modifier.padding(vertical = Ink.spacing.lg),
                )
            }
        }

        if (!people.isNullOrEmpty()) {
            items(people, key = { it.tag.id }) { TagRow(it, onClick = { onOpenTag(it.tag) }) }
        }
        if (!events.isNullOrEmpty()) {
            item(key = "events-header") { InkSectionHeader("Acontecimentos", Modifier.padding(top = Ink.spacing.xl, bottom = Ink.spacing.xs)) }
            items(events, key = { it.tag.id }) { TagRow(it, onClick = { onOpenTag(it.tag) }) }
        }
    }
}

/** [TODAS] [‹ OUTUBRO 2026 ›]: a opção ativa fica em moldura dupla. */
@Composable
private fun PeriodPicker(
    month: YearMonth?,
    onAll: () -> Unit,
    onMonth: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showing = month ?: YearMonth.now()
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        InkTextButton(
            "Todas",
            onClick = onAll,
            frame = if (month == null) InkFrameStyle.Double else InkFrameStyle.Single,
            color = if (month == null) Ink.colors.ink else Ink.colors.inkSoft,
        )
        Spacer(Modifier.width(Ink.spacing.sm))
        InkFrame(
            modifier = Modifier.weight(1f),
            style = if (month != null) InkFrameStyle.Double else InkFrameStyle.Single,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Arrow("‹", "Mês anterior", enabled = true, onClick = onPrevious)
                Text(
                    formatMonth(showing).uppercase(),
                    style = Ink.type.label,
                    color = if (month != null) Ink.colors.ink else Ink.colors.inkSoft,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).clickable(onClickLabel = "Ver este mês", onClick = onMonth).padding(vertical = 14.dp),
                )
                Arrow("›", "Próximo mês", enabled = showing < YearMonth.now(), onClick = onNext)
            }
        }
    }
}

@Composable
private fun Arrow(symbol: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        symbol,
        style = Ink.type.personName.copy(fontSize = Ink.type.section.fontSize * 1.4f, lineHeight = Ink.type.section.lineHeight),
        color = Ink.colors.ink,
        modifier = Modifier
            .alpha(if (enabled) 1f else InkSizes.MISSED_ALPHA)
            .clickable(enabled = enabled, onClickLabel = description, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

/** Nome + "6 NOTAS · ÚLTIMA 13 OUT", com linha de tinta embaixo. */
@Composable
private fun TagRow(item: TagWithCount, onClick: () -> Unit) {
    val isEvent = item.tag.type == TagType.EVENT
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(vertical = Ink.spacing.md), verticalAlignment = Alignment.Bottom) {
            Text(
                item.tag.name,
                style = Ink.type.section.copy(fontStyle = if (isEvent) FontStyle.Italic else FontStyle.Normal),
                color = if (isEvent) Ink.colors.seal else Ink.colors.ink,
                modifier = Modifier.weight(1f),
            )
            InkLabel(
                notesCount(item.noteCount) + (item.lastNoteAt?.let { " · última ${formatShortDay(it)}" } ?: ""),
            )
        }
        InkRule()
    }
}
