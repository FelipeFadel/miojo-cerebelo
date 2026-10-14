package com.miojo.cerebelo.ui.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miojo.cerebelo.ui.appViewModel
import com.miojo.cerebelo.ui.components.InkBackButton
import com.miojo.cerebelo.ui.components.InkDialog
import com.miojo.cerebelo.ui.components.InkFrame
import com.miojo.cerebelo.ui.components.InkFrameStyle
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.components.InkScreenTitle
import com.miojo.cerebelo.ui.components.InkTextButton
import com.miojo.cerebelo.ui.components.LedgerTextField
import com.miojo.cerebelo.ui.formatLongDay
import com.miojo.cerebelo.ui.formatTime
import com.miojo.cerebelo.ui.millisToDate
import com.miojo.cerebelo.ui.theme.Ink

/** Escrever (nova) ou editar uma nota. Nota nova abre com o teclado. */
@Composable
fun EditNoteScreen(noteId: Long?, withTagId: Long?, onClose: () -> Unit) {
    val vm = appViewModel { EditNoteViewModel(noteId, withTagId, it.noteRepository) }
    val state by vm.state.collectAsStateWithLifecycle()
    val tags by vm.tags.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var confirmLeave by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.done) { if (state.done) onClose() }

    // Nota nova: cursor no campo e teclado aberto.
    LaunchedEffect(state.loaded) {
        if (!state.loaded || !state.isNew) return@LaunchedEffect
        repeat(10) {
            if (runCatching { focusRequester.requestFocus() }.isSuccess) {
                keyboard?.show()
                return@LaunchedEffect
            }
            withFrameNanos { }
        }
    }

    val leave = { if (state.dirty && state.canSave) confirmLeave = true else onClose() }
    BackHandler(onBack = leave)

    Column(
        Modifier
            .fillMaxSize()
            .background(Ink.colors.page)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Ink.spacing.screen),
    ) {
        Row(Modifier.fillMaxWidth().padding(top = Ink.spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            InkBackButton(onClick = leave)
            Spacer(Modifier.weight(1f))
            if (!state.isNew) InkTextButton("Apagar", onClick = { confirmDelete = true }, color = Ink.colors.seal)
        }
        if (!state.loaded) return@Column

        InkScreenTitle(if (state.isNew) "Nova nota" else "Nota", Modifier.padding(top = Ink.spacing.sm))
        InkLabel(
            "${formatLongDay(millisToDate(state.createdAt))} · ${formatTime(state.createdAt)}",
            modifier = Modifier.padding(top = Ink.spacing.xs),
        )

        InkFrame(Modifier.fillMaxWidth().padding(top = Ink.spacing.lg), style = InkFrameStyle.Double) {
            Column(verticalArrangement = Arrangement.spacedBy(Ink.spacing.md)) {
                LedgerTextField(
                    value = state.text,
                    onValueChange = vm::onTextChange,
                    placeholder = "O que te contaram?",
                    minRows = 6,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
                TagEditorRow(
                    selected = state.tags,
                    allTags = tags,
                    onAdd = vm::addTag,
                    onRemove = vm::removeTag,
                )
            }
        }

        Row(Modifier.fillMaxWidth().padding(top = Ink.spacing.lg), horizontalArrangement = Arrangement.End) {
            InkTextButton("Guardar", onClick = vm::save, frame = InkFrameStyle.Double, enabled = state.canSave)
        }
        Spacer(Modifier.navigationBarsPadding().height(Ink.spacing.xl))
    }

    if (confirmDelete) {
        InkDialog(
            title = "Apagar esta nota?",
            onDismiss = { confirmDelete = false },
            confirmLabel = "Apagar",
            onConfirm = { confirmDelete = false; vm.delete() },
        ) {
            Text("Não dá para desfazer.", style = Ink.type.item, color = Ink.colors.inkSoft)
        }
    }

    if (confirmLeave) {
        InkDialog(
            title = "Guardar a nota?",
            onDismiss = { confirmLeave = false },
            dismissLabel = "Descartar",
            onDismissButton = { confirmLeave = false; onClose() },
            confirmLabel = "Guardar",
            onConfirm = { confirmLeave = false; vm.save() },
        )
    }
}
