package com.miojo.cerebelo.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.miojo.cerebelo.data.db.NoteWithTags
import com.miojo.cerebelo.data.db.TagEntity
import com.miojo.cerebelo.ui.theme.spacing

/**
 * Cartão de uma nota. [header] é o texto pequeno do topo
 * (hora na linha do tempo, data completa na tela de pessoa).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteCard(
    item: NoteWithTags,
    header: String,
    onClick: () -> Unit,
    onTagClick: (TagEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(MaterialTheme.spacing.lg)) {
            Text(
                text = header,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = item.note.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = MaterialTheme.spacing.xs),
            )
            if (item.tags.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    item.tags.forEach { tag ->
                        TagChip(tag.name, tag.type, onClick = { onTagClick(tag) })
                    }
                }
            }
        }
    }
}
