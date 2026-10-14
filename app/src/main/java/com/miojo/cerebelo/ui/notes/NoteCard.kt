package com.miojo.cerebelo.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miojo.cerebelo.data.db.NoteWithTags
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.ui.components.InkFrame
import com.miojo.cerebelo.ui.components.InkFrameStyle
import com.miojo.cerebelo.ui.components.InkLabel
import com.miojo.cerebelo.ui.components.InkTag
import com.miojo.cerebelo.ui.components.LedgerText
import com.miojo.cerebelo.ui.theme.Ink

/**
 * Cartão de nota: texto, tags embaixo à esquerda e [whenLabel] à direita.
 * [ledger] = moldura dupla com linhas de caderno (tela Notas);
 * sem ledger = moldura simples (linha do tempo).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    item: NoteWithTags,
    whenLabel: String,
    onClick: () -> Unit,
    onTagClick: (TagEntity) -> Unit,
    modifier: Modifier = Modifier,
    ledger: Boolean = false,
) {
    InkFrame(
        modifier = modifier.fillMaxWidth(),
        style = if (ledger) InkFrameStyle.Double else InkFrameStyle.Single,
        contentPadding = if (ledger) PaddingValues(start = 26.dp, end = 26.dp, top = 22.dp, bottom = 20.dp)
        else PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        onClick = onClick,
        onClickLabel = "Abrir nota",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Ink.spacing.sm)) {
            if (ledger) {
                LedgerText(item.note.text, maxLines = 8)
            } else {
                Text(item.note.text, style = Ink.type.body, color = Ink.colors.ink, maxLines = 8)
            }
            // A hora fica na última linha de tags, encostada à direita.
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Ink.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Ink.spacing.xs),
            ) {
                item.tags.forEach { tag -> InkTag(tag.name, tag.type, onClick = { onTagClick(tag) }) }
                Box(
                    Modifier
                        .align(Alignment.CenterVertically)
                        .widthIn(min = 56.dp)
                        .weight(1f),
                    contentAlignment = Alignment.CenterEnd,
                ) { InkLabel(whenLabel) }
            }
        }
    }
}
