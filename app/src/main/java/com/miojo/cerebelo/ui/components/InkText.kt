package com.miojo.cerebelo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.miojo.cerebelo.ui.theme.Ink
import com.miojo.cerebelo.ui.theme.InkAssets
import kotlin.math.roundToInt

// ---------- Linhas de caderno ----------

/**
 * Desenha uma linha de caderno sob cada linha de texto do [layout],
 * e mais linhas vazias até completar [minRows].
 */
private fun DrawScope.drawLedger(ledger: ImageBitmap, layout: TextLayoutResult?, rowHeightPx: Float, minRows: Int) {
    val bottoms = mutableListOf<Float>()
    if (layout != null) for (i in 0 until layout.lineCount) bottoms += layout.getLineBottom(i)
    var last = bottoms.lastOrNull() ?: 0f
    while (bottoms.size < minRows) {
        last += rowHeightPx
        bottoms += last
    }
    val width = size.width.roundToInt()
    val height = rowHeightPx.roundToInt()
    bottoms.forEach { bottom ->
        drawImage(
            image = ledger,
            dstOffset = IntOffset(0, (bottom - rowHeightPx).roundToInt()),
            dstSize = IntSize(width, height),
        )
    }
}

/** Texto de nota com linhas de caderno atrás (alinhadas ao lineHeight do corpo). */
@Composable
fun LedgerText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = Ink.type.body,
    maxLines: Int = Int.MAX_VALUE,
) {
    val ledger = ImageBitmap.imageResource(InkAssets.ledger)
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = text,
        style = style,
        color = Ink.colors.ink,
        maxLines = maxLines,
        onTextLayout = { layout = it },
        modifier = modifier.drawBehind {
            drawLedger(ledger, layout, style.lineHeight.toPx(), minRows = 0)
        },
    )
}

/** Campo de escrita de nota: texto sobre linhas de caderno, sem moldura própria. */
@Composable
fun LedgerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    minRows: Int = 6,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val ledger = ImageBitmap.imageResource(InkAssets.ledger)
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val style = Ink.type.body.copy(color = Ink.colors.ink)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = style,
        cursorBrush = SolidColor(Ink.colors.ink),
        keyboardOptions = keyboardOptions,
        minLines = minRows,
        onTextLayout = { layout = it },
        modifier = modifier.drawBehind {
            drawLedger(ledger, layout, style.lineHeight.toPx(), minRows)
        },
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) Text(placeholder, style = style, color = Ink.colors.inkSoft)
                inner()
            }
        },
    )
}

/** Campo de uma linha dentro de moldura simples (busca, nomes). */
@Composable
fun InkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    showSearchIcon: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailing: (@Composable () -> Unit)? = null,
) {
    val style = Ink.type.item.copy(color = Ink.colors.ink)
    InkFrame(
        modifier = modifier.fillMaxWidth(),
        style = InkFrameStyle.Single,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showSearchIcon) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Ink.colors.ink, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = style,
                singleLine = true,
                cursorBrush = SolidColor(Ink.colors.ink),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty()) Text(placeholder, style = style, color = Ink.colors.inkSoft, maxLines = 1)
                        inner()
                    }
                },
            )
            if (trailing != null) trailing()
        }
    }
}

// ---------- Texto de afazer ----------

/** Texto de item de checklist; marcado = riscado em tom secundário. */
@Composable
fun InkItemText(text: String, checked: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = Ink.type.item.copy(textDecoration = if (checked) TextDecoration.LineThrough else null),
        color = if (checked) Ink.colors.inkSoft else Ink.colors.ink,
        modifier = modifier,
    )
}

// ---------- Diálogo ----------

/** Diálogo em papel com moldura dupla (substitui o AlertDialog arredondado do Material). */
@Composable
fun InkDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String? = null,
    onConfirm: () -> Unit = {},
    confirmEnabled: Boolean = true,
    dismissLabel: String = "Cancelar",
    content: @Composable () -> Unit = {},
) {
    Dialog(onDismissRequest = onDismiss) {
        val frame = rememberInkDrawable(InkFrameStyle.Double.res)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink.colors.page)
                .drawNinePatch(frame)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(Ink.spacing.md),
        ) {
            Text(title, style = Ink.type.section, color = Ink.colors.ink)
            content()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                InkTextButton(dismissLabel, onClick = onDismiss, color = Ink.colors.inkSoft)
                if (confirmLabel != null) {
                    Spacer(Modifier.width(Ink.spacing.sm))
                    InkTextButton(confirmLabel, onClick = onConfirm, frame = InkFrameStyle.Single, enabled = confirmEnabled)
                }
            }
        }
    }
}
