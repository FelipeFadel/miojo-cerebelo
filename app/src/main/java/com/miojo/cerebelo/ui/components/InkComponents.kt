package com.miojo.cerebelo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.miojo.cerebelo.data.db.TagType
import com.miojo.cerebelo.ui.theme.Ink
import com.miojo.cerebelo.ui.theme.InkAssets
import com.miojo.cerebelo.ui.theme.InkSizes
import java.time.LocalDate
import kotlin.random.Random

// ---------- Traços ----------

/** Linha de tinta horizontal (divisórias, topo da navegação). */
@Composable
fun InkRule(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(InkAssets.rule),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier.fillMaxWidth().height(InkSizes.ruleThickness),
    )
}

/** Linha de tinta vertical (linha do tempo da pessoa, faixa do widget). */
@Composable
fun InkVRule(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(InkAssets.vrule),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier.width(InkSizes.vruleThickness).fillMaxHeight(),
    )
}

// ---------- Checkbox ----------

/**
 * Checkbox de tinta: caixa desenhada + X vermelho quando marcado.
 * Com [onCheckedChange] nulo vira só desenho (ex.: dias da sequência).
 */
@Composable
fun InkCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val toggle = if (onCheckedChange != null) {
        Modifier
            .minimumInteractiveComponentSize()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
    } else Modifier
    Box(modifier.then(toggle), contentAlignment = Alignment.Center) {
        Box(Modifier.size(InkSizes.checkbox).alpha(alpha)) {
            Image(painterResource(InkAssets.box), contentDescription = null, modifier = Modifier.size(InkSizes.checkbox))
            if (checked) {
                Image(painterResource(InkAssets.xmark), contentDescription = null, modifier = Modifier.size(InkSizes.checkbox))
            }
        }
    }
}

// ---------- Tags ----------

/**
 * Tag sem preenchimento: só moldura + texto.
 * Pessoa = moldura oliva e texto reto; acontecimento = moldura vermelha e texto itálico.
 */
@Composable
fun InkTag(
    name: String,
    type: TagType,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val isPerson = type == TagType.PERSON
    val label = name.uppercase() + (trailing?.let { "  $it" } ?: "")
    InkFrame(
        modifier = modifier.then(if (onClick != null) Modifier.minimumInteractiveComponentSize() else Modifier),
        style = if (isPerson) InkFrameStyle.Person else InkFrameStyle.Event,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
        onClick = onClick,
    ) {
        Text(
            text = label,
            style = Ink.type.label.copy(fontStyle = if (isPerson) FontStyle.Normal else FontStyle.Italic),
            color = if (isPerson) Ink.colors.person else Ink.colors.seal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 220.dp),
        )
    }
}

// ---------- Botões ----------

/** Botão "+" quadrado de moldura dupla. */
@Composable
fun InkPlusButton(onClick: () -> Unit, modifier: Modifier = Modifier, contentDescription: String = "Nova nota") {
    InkFrame(
        modifier = modifier.size(InkSizes.plusButton),
        style = InkFrameStyle.Double,
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
        onClickLabel = contentDescription,
    ) {
        Image(
            painter = painterResource(InkAssets.plus),
            contentDescription = contentDescription,
            modifier = Modifier.size(InkSizes.plusIcon).align(Alignment.Center),
        )
    }
}

/** Botão de texto em MAIÚSCULAS, opcionalmente dentro de uma moldura. */
@Composable
fun InkTextButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    frame: InkFrameStyle? = null,
    color: Color = Ink.colors.ink,
    italic: Boolean = false,
    enabled: Boolean = true,
) {
    val text: @Composable () -> Unit = {
        Text(
            text = label.uppercase(),
            style = Ink.type.label.copy(fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal),
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
    val faded = if (enabled) 1f else InkSizes.MISSED_ALPHA
    if (frame != null) {
        InkFrame(
            modifier = modifier.minimumInteractiveComponentSize().alpha(faded),
            style = frame,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            onClick = if (enabled) onClick else null,
        ) { Box(Modifier.align(Alignment.Center)) { text() } }
    } else {
        Box(
            modifier = modifier
                .minimumInteractiveComponentSize()
                .alpha(faded)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) { text() }
    }
}

/** Seta de voltar desenhada fina, sem fundo. */
@Composable
fun InkBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(role = Role.Button, onClickLabel = "Voltar", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Voltar", tint = Ink.colors.ink, modifier = Modifier.size(32.dp))
    }
}

// ---------- Textos de página ----------

/** Título de tela em MAIÚSCULAS. */
@Composable
fun InkScreenTitle(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(text.uppercase(), style = Ink.type.screenTitle, color = Ink.colors.ink, textAlign = textAlign, modifier = modifier)
}

/** Rótulo pequeno em MAIÚSCULAS (data, contador, hora). */
@Composable
fun InkLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Ink.colors.inkSoft,
    textAlign: TextAlign? = null,
    style: TextStyle = Ink.type.label,
) {
    Text(text.uppercase(), style = style, color = color, textAlign = textAlign, modifier = modifier)
}

/** Título de seção seguido de uma linha de tinta até a margem: "Afazeres ————". */
@Composable
fun InkSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = Ink.type.section, color = Ink.colors.ink)
        Spacer(Modifier.width(Ink.spacing.md))
        InkRule(Modifier.weight(1f))
        if (trailing != null) {
            Spacer(Modifier.width(Ink.spacing.sm))
            trailing()
        }
    }
}

/** Ilustração do cabeçalho, sorteada por dia (a mesma o dia todo). */
@Composable
fun OrnamentOfDay(modifier: Modifier = Modifier, date: LocalDate = LocalDate.now()) {
    val pool = InkAssets.headerOrnaments
    val res = pool[Random(date.toEpochDay()).nextInt(pool.size)]
    Image(
        painter = painterResource(res),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.fillMaxWidth().height(InkSizes.ornamentHeight),
    )
}

// ---------- Navegação ----------

/** Navegação inferior: linha de tinta em cima e itens só de texto; o ativo é sublinhado. */
@Composable
fun InkBottomNav(
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().navigationBarsPadding()) {
        InkRule(Modifier.padding(horizontal = Ink.spacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Ink.spacing.screen, vertical = Ink.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            items.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val ink = Ink.colors.ink
                Box(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clickable(role = Role.Tab, onClick = { onSelect(index) })
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = Ink.type.nav,
                        color = if (selected) ink else Ink.colors.inkSoft,
                        modifier = if (selected) Modifier.drawBehind {
                            val y = size.height + 3.dp.toPx()
                            drawLine(ink, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.5.dp.toPx())
                        } else Modifier,
                    )
                }
            }
        }
    }
}

// ---------- Sequência ----------

/** Situação de um dia na faixa de sequência. */
enum class DayMark { DONE, MISSED, TODAY, FUTURE }

/** Um dia da faixa de sequência: caixa (+ X se cumprido) e a inicial embaixo. */
@Composable
fun InkDayBox(label: String, mark: DayMark, isToday: Boolean, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        InkCheckbox(
            checked = mark == DayMark.DONE,
            onCheckedChange = null,
            alpha = if (mark == DayMark.MISSED || mark == DayMark.FUTURE) InkSizes.MISSED_ALPHA else 1f,
        )
        Spacer(Modifier.height(Ink.spacing.xs))
        val ink = Ink.colors.ink
        Text(
            text = label.uppercase(),
            style = Ink.type.label,
            color = if (isToday) ink else Ink.colors.inkSoft,
            maxLines = 1,
            modifier = if (isToday) Modifier.drawBehind {
                val y = size.height + 1.dp.toPx()
                drawLine(ink, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.2.dp.toPx())
            } else Modifier,
        )
    }
}
