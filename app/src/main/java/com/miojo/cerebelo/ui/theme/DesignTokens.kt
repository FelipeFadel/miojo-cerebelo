package com.miojo.cerebelo.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miojo.cerebelo.R

/*
 * ============================================================
 *  DESIGN TOKENS — "Manuscrito iluminado"
 *  Todo o visual do app (e do widget) sai deste arquivo.
 *  Traços e ilustrações claro/escuro são escolhidos pelo próprio
 *  Android: res/drawable-* (tema claro) e res/drawable-night-* (escuro).
 * ============================================================
 */

// ---------- CORES ----------

/** Papéis de cor usados pelas telas. */
data class InkColors(
    /** Fundo da página (Paper / Plate). */
    val page: Color,
    /** Texto principal (Ink / Parchment). */
    val ink: Color,
    /** Texto secundário, hora, itens inativos (InkSoft / ParchmentDim). */
    val inkSoft: Color,
    /** Traços desenhados em código (Sepia / Gilt). */
    val stroke: Color,
    /** Tag de pessoa (Olive / OliveLight). */
    val person: Color,
    /** Tag de acontecimento, número da sequência, X marcado (Seal / SealLight). */
    val seal: Color,
)

private object Palette {
    // Tema claro
    val Paper = Color(0xFFF3EBD0)
    val Ink = Color(0xFF221E17)
    val InkSoft = Color(0xFF5A4F3C)
    val Sepia = Color(0xFF3A2E20)
    val Olive = Color(0xFF4E5242)
    val Seal = Color(0xFF8C3A2B)

    // Tema escuro
    val Plate = Color(0xFF14130F)
    val Parchment = Color(0xFFE9DFC4)
    val ParchmentDim = Color(0xFFA89F86)
    val Gilt = Color(0xFFC9B27C)
    val OliveLight = Color(0xFFB4B8A4)
    val SealLight = Color(0xFFD88A78)
}

internal val LightInk = InkColors(
    page = Palette.Paper,
    ink = Palette.Ink,
    inkSoft = Palette.InkSoft,
    stroke = Palette.Sepia,
    person = Palette.Olive,
    seal = Palette.Seal,
)

internal val DarkInk = InkColors(
    page = Palette.Plate,
    ink = Palette.Parchment,
    inkSoft = Palette.ParchmentDim,
    stroke = Palette.Gilt,
    person = Palette.OliveLight,
    seal = Palette.SealLight,
)

// ---------- FONTES ----------

/** IM Fell English: títulos e navegação. */
internal val FellFamily = FontFamily(
    Font(R.font.im_fell_english, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.im_fell_english_italic, FontWeight.Normal, FontStyle.Italic),
)

/** EB Garamond (fonte variável): texto. */
@OptIn(ExperimentalTextApi::class)
internal val GaramondFamily = FontFamily(
    Font(R.font.eb_garamond, FontWeight.Normal, FontStyle.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.eb_garamond, FontWeight.Medium, FontStyle.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.eb_garamond, FontWeight.SemiBold, FontStyle.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.eb_garamond_italic, FontWeight.Normal, FontStyle.Italic, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
)

// ---------- TIPOGRAFIA ----------

/** Estilos de texto. Os marcados "MAIÚSCULAS" devem receber texto já em caixa alta. */
data class InkType(
    /** Título de tela (MAIÚSCULAS): "NOTAS", "AFAZERES". */
    val screenTitle: TextStyle,
    /** Nome da pessoa no topo da tela dela. */
    val personName: TextStyle,
    /** Título de seção: "Afazeres", "Hoje · 13 out". */
    val section: TextStyle,
    /** Corpo da nota; o lineHeight acompanha a linha de caderno. */
    val body: TextStyle,
    /** Texto de listas (afazeres, pessoas). */
    val item: TextStyle,
    /** Tags, hora e rótulos (MAIÚSCULAS). */
    val label: TextStyle,
    /** Itens da navegação inferior. */
    val nav: TextStyle,
    /** Número grande da sequência. */
    val bigNumber: TextStyle,
    /** Datas na linha do tempo da pessoa ("13"). */
    val dateNumber: TextStyle,
)

internal val AppInkType = InkType(
    screenTitle = TextStyle(fontFamily = FellFamily, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = 3.sp),
    personName = TextStyle(fontFamily = FellFamily, fontSize = 44.sp, lineHeight = 52.sp, letterSpacing = 1.sp),
    section = TextStyle(fontFamily = FellFamily, fontSize = 20.sp, lineHeight = 26.sp),
    body = TextStyle(
        fontFamily = GaramondFamily, fontSize = 18.sp, lineHeight = 28.sp,
        // Toda linha com exatamente 28sp, para casar com a linha de caderno.
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
    ),
    item = TextStyle(fontFamily = GaramondFamily, fontSize = 18.sp, lineHeight = 24.sp),
    label = TextStyle(fontFamily = GaramondFamily, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 1.2.sp),
    nav = TextStyle(fontFamily = FellFamily, fontSize = 18.sp, lineHeight = 24.sp),
    bigNumber = TextStyle(fontFamily = FellFamily, fontStyle = FontStyle.Italic, fontSize = 52.sp, lineHeight = 56.sp),
    dateNumber = TextStyle(fontFamily = FellFamily, fontSize = 20.sp, lineHeight = 22.sp),
)

// ---------- ESPAÇAMENTOS E TAMANHOS ----------

data class InkSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    /** Margem lateral das telas. */
    val screen: Dp = 20.dp,
    /** Alvo de toque mínimo. */
    val touch: Dp = 44.dp,
    /** Respiro no topo da tela de Notas, para o dragão. */
    val dragonClearance: Dp = 96.dp,
)

internal val AppInkSpacing = InkSpacing()

/** Tamanhos dos elementos desenhados. */
internal object InkSizes {
    val plusButton = 60.dp
    val plusIcon = 20.dp
    val checkbox = 28.dp
    val ruleThickness = 4.dp
    val vruleThickness = 4.dp
    /** Altura de 1 linha de caderno (igual ao lineHeight do corpo da nota). */
    val ledgerRow = 28.dp
    val ornamentHeight = 96.dp
    val dragonWidth = 300.dp
    val dragonOffsetX = 12.dp
    val dragonOffsetY = (-4).dp
    /** Opacidade de um dia perdido / futuro na sequência. */
    const val MISSED_ALPHA = 0.35f
}

// ---------- ASSETS (imagens) ----------

/** Traços de tinta e ilustrações. A versão do tema escuro vem de drawable-night-*. */
internal object InkAssets {
    val frameDouble = R.drawable.ink_frame2
    val frameSingle = R.drawable.ink_frame1
    val framePerson = R.drawable.ink_frame1_person
    val frameEvent = R.drawable.ink_frame1_event
    val box = R.drawable.ink_box
    val xmark = R.drawable.ink_xmark
    val rule = R.drawable.ink_rule
    val vrule = R.drawable.ink_vrule
    val ledger = R.drawable.ink_ledger
    val plus = R.drawable.ink_plus

    val dragon = R.drawable.orn_canto_dragao

    /** Ilustrações sorteadas por dia no cabeçalho de Tempo, Pessoa e Afazeres. */
    val headerOrnaments = listOf(
        R.drawable.orn_cabecalho_sereias,
        R.drawable.orn_cabecalho_folhagem,
        R.drawable.orn_rodape_sereias,
        R.drawable.orn_medalhao_bosque,
        R.drawable.orn_medalhao_inverno,
        R.drawable.orn_medalhao_lago,
        R.drawable.orn_medalhao_navio,
        R.drawable.orn_quadro_igreja,
        R.drawable.orn_quadro_paisagem_betulas,
        R.drawable.orn_quadro_paisagem_colina,
        R.drawable.orn_selo_ouroboros,
        R.drawable.orn_vinheta_coelho_cavaleiro,
    )

    /** Ilustrações verticais da faixa lateral do widget: uma por dia da semana, de segunda a domingo. */
    val widgetVerticals = listOf(
        R.drawable.orn_vertical_conchas,
        R.drawable.orn_vertical_entrelacado,
        R.drawable.orn_vertical_flores_lateral_a,
        R.drawable.orn_vertical_flores_lateral_b,
        R.drawable.orn_vertical_jardim_relogio_de_sol,
        R.drawable.orn_vertical_narcisos,
        R.drawable.orn_vertical_sinos,
    )
}

// ---------- WIDGET ----------

/** Tamanhos do widget (o Glance não aceita fonte própria: usa Serif do sistema). */
internal object WidgetTokens {
    val padding = 16.dp
    val itemSpacing = 6.dp
    val sideStripWidth = 120.dp
    val cornerRadius = 16.dp
    val buttonHeight = 36.dp
    val plusIcon = 16.dp
    val checkbox = 24.dp
    val ruleHeight = 6.dp
    val labelSize = 11.sp
    val bodySize = 15.sp
    val itemSize = 16.sp
}
