package com.miojo.cerebelo.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.data.db.NoteWithTags
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.data.db.TagType
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.components.InkBackButton
import com.miojo.cerebelo.ui.components.InkDialog
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.components.InkPlusButton
import com.miojo.cerebelo.ui.components.InkTag
import com.miojo.cerebelo.ui.components.InkTextButton
import com.miojo.cerebelo.ui.components.InkTextField
import com.miojo.cerebelo.ui.components.InkVRule
import com.miojo.cerebelo.ui.components.OrnamentOfDay
import com.miojo.cerebelo.ui.formatDayNumber
import com.miojo.cerebelo.ui.formatMonthShort
import com.miojo.cerebelo.ui.formatSince
import com.miojo.cerebelo.ui.notesCount
import com.miojo.cerebelo.ui.theme.Ink
import com.miojo.cerebelo.ui.theme.InkSizes

/** Tela de uma pessoa (ou acontecimento): linha do tempo vertical, mais recentes primeiro. */
@Composable
fun TagScreen(
    tagId: Long,
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onTagClick: (TagEntity) -> Unit,
    onNewNote: (TagEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm = appViewModel { TagViewModel(tagId, it.noteRepository) }
    val tag by vm.tag.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val deleted by vm.deleted.collectAsStateWithLifecycle()
    val renameError by vm.renameError.collectAsStateWithLifecycle()
    var showRename by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(deleted) { if (deleted) onBack() }

    Box(modifier.fillMaxSize()) {
        val current = tag
        val list = notes
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = Ink.spacing.screen, end = Ink.spacing.screen, bottom = InkSizes.plusButton + Ink.spacing.xl),
        ) {
            item(key = "header") {
                Column(Modifier.statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth()) { InkBackButton(onClick = onBack) }
                    OrnamentOfDay()
                    if (current != null) {
                        val isEvent = current.type == TagType.EVENT
                        Text(
                            current.name,
                            style = Ink.type.personName.copy(fontStyle = if (isEvent) FontStyle.Italic else FontStyle.Normal),
                            color = if (isEvent) Ink.colors.seal else Ink.colors.ink,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = Ink.spacing.sm),
                        )
                    }
                    if (list != null) {
                        InkLabel(
                            when {
                                list.isEmpty() -> "Nenhuma nota"
                                else -> "${notesCount(list.size)} · desde ${formatSince(list.last().note.createdAt)}"
                            },
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = Ink.spacing.xs, bottom = Ink.spacing.lg),
                        )
                    }
                }
            }

            itemsIndexed(list.orEmpty(), key = { _, it -> it.note.id }) { index, item ->
                TimelineEntry(
                    item = item,
                    tagId = tagId,
                    isLast = index == list!!.lastIndex,
                    onOpen = { onOpenNote(item.note.id) },
                    onTagClick = onTagClick,
                )
            }

            if (current != null) {
                item(key = "actions") {
                    Row(
                        Modifier.fillMaxWidth().padding(top = Ink.spacing.lg),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        InkTextButton("Renomear", onClick = { showRename = true }, color = Ink.colors.inkSoft)
                        InkLabel("·")
                        InkTextButton("Apagar tag", onClick = { confirmDelete = true }, color = Ink.colors.inkSoft)
                    }
                }
            }
        }

        if (current != null) {
            InkPlusButton(
                onClick = { onNewNote(current) },
                contentDescription = "Nova nota sobre ${current.name}",
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = Ink.spacing.screen, bottom = Ink.spacing.md),
            )
        }
    }

    val current = tag
    if (showRename && current != null) {
        RenameTagDialog(
            tag = current,
            showError = renameError,
            onRename = { vm.rename(it) { showRename = false } },
            onDismiss = { showRename = false; vm.clearRenameError() },
        )
    }

    if (confirmDelete) {
        InkDialog(
            title = "Apagar a tag?",
            onDismiss = { confirmDelete = false },
            confirmLabel = "Apagar",
            onConfirm = { confirmDelete = false; vm.delete() },
        ) {
            Text(
                "As notas continuam guardadas, só perdem esta marcação.",
                style = Ink.type.item,
                color = Ink.colors.inkSoft,
            )
        }
    }
}

/** Uma nota na linha do tempo da pessoa: data | ponto + linha | texto e tags. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimelineEntry(
    item: NoteWithTags,
    tagId: Long,
    isLast: Boolean,
    onOpen: () -> Unit,
    onTagClick: (TagEntity) -> Unit,
) {
    val createdAt = item.note.createdAt
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClickLabel = "Abrir nota", onClick = onOpen),
    ) {
        Column(Modifier.width(44.dp), horizontalAlignment = Alignment.End) {
            Text(formatDayNumber(createdAt), style = Ink.type.dateNumber, color = Ink.colors.ink)
            InkLabel(formatMonthShort(createdAt))
        }
        Column(
            Modifier.width(36.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.padding(top = 6.dp).size(10.dp).clip(CircleShape).background(Ink.colors.seal))
            if (!isLast) InkVRule(Modifier.weight(1f).padding(top = 2.dp))
        }
        Column(Modifier.weight(1f).padding(bottom = Ink.spacing.lg), verticalArrangement = Arrangement.spacedBy(Ink.spacing.sm)) {
            Text(item.note.text, style = Ink.type.body, color = Ink.colors.ink)
            val others = item.tags.filter { it.id != tagId }
            if (others.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Ink.spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Ink.spacing.xs),
                ) {
                    others.forEach { InkTag(it.name, it.type, onClick = { onTagClick(it) }) }
                }
            }
        }
    }
}

@Composable
private fun RenameTagDialog(
    tag: TagEntity,
    showError: Boolean,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(tag.name) }
    InkDialog(
        title = "Renomear",
        onDismiss = onDismiss,
        confirmLabel = "Guardar",
        onConfirm = { onRename(name) },
        confirmEnabled = name.isNotBlank(),
    ) {
        Column {
            InkTextField(
                value = name,
                onValueChange = { name = it },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )
            if (showError) {
                Spacer(Modifier.height(Ink.spacing.xs))
                Text("Já existe uma tag com esse nome.", style = Ink.type.label, color = Ink.colors.seal)
            }
        }
    }
}
