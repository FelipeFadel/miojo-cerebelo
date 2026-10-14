package com.miojo.cerebelo.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.data.db.TagType
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.components.InkFrameStyle
import com.miojo.cerebelo.ui.components.InkScreenTitle
import com.miojo.cerebelo.ui.components.InkSectionHeader
import com.miojo.cerebelo.ui.components.InkTextButton
import com.miojo.cerebelo.ui.components.InkTextField
import com.miojo.cerebelo.ui.components.OrnamentOfDay
import com.miojo.cerebelo.ui.formatGroupDay
import com.miojo.cerebelo.ui.formatTime
import com.miojo.cerebelo.ui.theme.Ink

/** "Tempo": busca, filtros e notas agrupadas por dia. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimelineScreen(
    onOpenNote: (Long) -> Unit,
    onTagClick: (TagEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = appViewModel { TimelineViewModel(it.noteRepository) }
    val tags by vm.tags.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val filterTag by vm.filterTag.collectAsStateWithLifecycle()
    val timeline by vm.timeline.collectAsStateWithLifecycle()
    var pickerType by rememberSaveable { mutableStateOf<TagType?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Ink.spacing.screen, end = Ink.spacing.screen, bottom = Ink.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Ink.spacing.md),
    ) {
        item(key = "header") {
            Column(Modifier.statusBarsPadding().padding(top = Ink.spacing.md)) {
                OrnamentOfDay()
                InkScreenTitle("Linha do tempo", Modifier.fillMaxWidth().padding(top = Ink.spacing.sm), textAlign = TextAlign.Center)
            }
        }

        item(key = "search") {
            InkTextField(
                value = query,
                onValueChange = vm::onQueryChange,
                placeholder = "Buscar por nome, tag ou palavra",
                showSearchIcon = true,
                trailing = if (query.isNotEmpty()) {
                    {
                        Text(
                            "×",
                            style = Ink.type.section,
                            color = Ink.colors.inkSoft,
                            modifier = Modifier.clickable(onClickLabel = "Limpar busca") { vm.onQueryChange("") }.padding(horizontal = 8.dp),
                        )
                    }
                } else null,
            )
        }

        item(key = "filters") {
            val current = filterTag
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Ink.spacing.sm)) {
                InkTextButton(
                    "Tudo",
                    onClick = { vm.setFilter(null) },
                    frame = if (current == null) InkFrameStyle.Double else InkFrameStyle.Single,
                    color = if (current == null) Ink.colors.ink else Ink.colors.inkSoft,
                )
                FilterButton(TagType.PERSON, current, onPick = { pickerType = TagType.PERSON }, onClear = { vm.setFilter(null) })
                FilterButton(TagType.EVENT, current, onPick = { pickerType = TagType.EVENT }, onClear = { vm.setFilter(null) })
            }
        }

        val groups = timeline.groups
        if (groups != null && groups.isEmpty()) {
            item(key = "empty") {
                Text(
                    if (timeline.isFiltering) "Nada encontrado." else "Nenhuma nota ainda.",
                    style = Ink.type.item,
                    color = Ink.colors.inkSoft,
                    modifier = Modifier.padding(vertical = Ink.spacing.lg),
                )
            }
        }

        groups.orEmpty().forEach { group ->
            item(key = "day-${group.date.toEpochDay()}") {
                InkSectionHeader(formatGroupDay(group.date), Modifier.padding(top = Ink.spacing.sm))
            }
            items(group.notes, key = { it.note.id }) { item ->
                NoteCard(
                    item = item,
                    whenLabel = formatTime(item.note.createdAt),
                    onClick = { onOpenNote(item.note.id) },
                    onTagClick = onTagClick,
                )
            }
        }
    }

    pickerType?.let { type ->
        TagPickerDialog(
            title = if (type == TagType.PERSON) "Ver notas de quem?" else "Qual acontecimento?",
            tags = tags.filter { it.type == type },
            createType = null,
            onPick = { picked -> vm.setFilter(picked.id); pickerType = null },
            onDismiss = { pickerType = null },
        )
    }
}

/** "PESSOAS" / "ACONTECIMENTOS"; com filtro ativo mostra o nome escolhido e "×" para limpar. */
@Composable
private fun FilterButton(type: TagType, current: TagEntity?, onPick: () -> Unit, onClear: () -> Unit) {
    val isPerson = type == TagType.PERSON
    val active = current?.type == type
    InkTextButton(
        label = if (active) "${current!!.name}  ×" else if (isPerson) "Pessoas" else "Acontecimentos",
        onClick = if (active) onClear else onPick,
        frame = if (isPerson) InkFrameStyle.Person else InkFrameStyle.Event,
        color = if (isPerson) Ink.colors.person else Ink.colors.seal,
        italic = !isPerson,
    )
}
