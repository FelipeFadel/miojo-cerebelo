package com.miojo.cerebelo.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.miojo.cerebelo.data.TagRef
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.ui.theme.spacing

/** Caixa de escrita do topo da tela inicial. */
@Composable
fun NoteComposer(
    state: ComposerState,
    allTags: List<TagEntity>,
    focusRequester: FocusRequester,
    onTextChange: (String) -> Unit,
    onAddTag: (TagRef) -> Unit,
    onRemoveTag: (TagRef) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(MaterialTheme.spacing.md)) {
            TextField(
                value = state.text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                placeholder = { Text("O que te contaram?") },
                minLines = 3,
                maxLines = 10,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
            TagEditorRow(
                selected = state.tags,
                allTags = allTags,
                onAdd = onAddTag,
                onRemove = onRemoveTag,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.xs),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = {
                        onSave()
                        focusManager.clearFocus()
                    },
                    enabled = state.canSave,
                ) { Text("Salvar") }
            }
        }
    }
}
