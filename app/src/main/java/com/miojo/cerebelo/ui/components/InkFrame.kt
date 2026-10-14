package com.miojo.cerebelo.ui.components

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.miojo.cerebelo.ui.theme.InkAssets
import kotlin.math.roundToInt

/** Molduras de tinta (9-patch). */
enum class InkFrameStyle(@DrawableRes val res: Int) {
    /** Moldura dupla: cards de nota, botão +, quadro da sequência. */
    Double(InkAssets.frameDouble),
    /** Moldura simples: busca, cards da linha do tempo. */
    Single(InkAssets.frameSingle),
    /** Moldura de tag de pessoa (oliva). */
    Person(InkAssets.framePerson),
    /** Moldura de tag de acontecimento (vermelha). */
    Event(InkAssets.frameEvent),
}

/** Carrega um drawable do tema atual (claro/escuro) e recarrega se o tema mudar. */
@Composable
fun rememberInkDrawable(@DrawableRes id: Int): Drawable {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    return remember(id, configuration) { requireNotNull(ContextCompat.getDrawable(context, id)) }
}

/** Desenha um 9-patch esticado no tamanho do elemento (o painterResource não entende 9-patch). */
fun Modifier.drawNinePatch(drawable: Drawable): Modifier = drawBehind {
    drawIntoCanvas { canvas ->
        drawable.setBounds(0, 0, size.width.roundToInt(), size.height.roundToInt())
        drawable.draw(canvas.nativeCanvas)
    }
}

/** Caixa com moldura de tinta. Sem fundo próprio: a moldura fica sobre o papel. */
@Composable
fun InkFrame(
    modifier: Modifier = Modifier,
    style: InkFrameStyle = InkFrameStyle.Double,
    contentPadding: PaddingValues = PaddingValues(if (style == InkFrameStyle.Double) 24.dp else 16.dp),
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val frame = rememberInkDrawable(style.res)
    Box(
        modifier = modifier
            .drawNinePatch(frame)
            .then(
                if (onClick != null) Modifier.clickable(onClickLabel = onClickLabel, role = Role.Button, onClick = onClick)
                else Modifier
            )
            .padding(contentPadding),
        content = content,
    )
}
