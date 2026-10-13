package com.miojo.cerebelo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * ============================================================
 *  DESIGN TOKENS — TODO o visual do app sai deste arquivo.
 *  Os valores abaixo são PROVISÓRIOS. Para mudar o visual,
 *  altere só aqui (o app e o widget leem estes valores).
 * ============================================================
 */

// ---------- CORES ----------

private object Palette {
    val indigo = Color(0xFF4F5BD5)
    val indigoLight = Color(0xFFBDC2FF)
    val indigoContainer = Color(0xFFE0E0FF)
    val indigoDeep = Color(0xFF151D7A)

    val teal = Color(0xFF00897B)
    val tealLight = Color(0xFF80CBC4)
    val tealContainer = Color(0xFFCCEDE8)
    val tealDeep = Color(0xFF00382F)

    val paper = Color(0xFFFBF8FF)
    val ink = Color(0xFF1B1B21)
    val surfaceLow = Color(0xFFF2EFF7)
    val surfaceHigh = Color(0xFFE9E7EF)
    val outline = Color(0xFF767680)

    val night = Color(0xFF131318)
    val nightText = Color(0xFFE4E1E9)
    val nightLow = Color(0xFF1F1F25)
    val nightHigh = Color(0xFF2A292F)

    val error = Color(0xFFBA1A1A)
    val errorLight = Color(0xFFFFB4AB)
}

internal val LightColors = lightColorScheme(
    primary = Palette.indigo,
    onPrimary = Color.White,
    primaryContainer = Palette.indigoContainer,
    onPrimaryContainer = Palette.indigoDeep,
    secondary = Palette.teal,
    onSecondary = Color.White,
    secondaryContainer = Palette.tealContainer,
    onSecondaryContainer = Palette.tealDeep,
    background = Palette.paper,
    onBackground = Palette.ink,
    surface = Palette.paper,
    onSurface = Palette.ink,
    surfaceVariant = Palette.surfaceHigh,
    onSurfaceVariant = Color(0xFF46464F),
    surfaceContainerLow = Palette.surfaceLow,
    surfaceContainer = Palette.surfaceLow,
    surfaceContainerHigh = Palette.surfaceHigh,
    outline = Palette.outline,
    error = Palette.error,
)

internal val DarkColors = darkColorScheme(
    primary = Palette.indigoLight,
    onPrimary = Palette.indigoDeep,
    primaryContainer = Color(0xFF353FAA),
    onPrimaryContainer = Palette.indigoContainer,
    secondary = Palette.tealLight,
    onSecondary = Palette.tealDeep,
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = Palette.tealContainer,
    background = Palette.night,
    onBackground = Palette.nightText,
    surface = Palette.night,
    onSurface = Palette.nightText,
    surfaceVariant = Palette.nightHigh,
    onSurfaceVariant = Color(0xFFC7C5D0),
    surfaceContainerLow = Palette.nightLow,
    surfaceContainer = Palette.nightLow,
    surfaceContainerHigh = Palette.nightHigh,
    outline = Color(0xFF90909A),
    error = Palette.errorLight,
)

// ---------- TIPOGRAFIA ----------

/** Troque por uma fonte própria (ex.: FontFamily(Font(R.font.minha_fonte))). */
private val AppFont = FontFamily.Default

internal val AppTypography = Typography(
    headlineSmall = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = AppFont, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

// ---------- FORMAS ----------

internal val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// ---------- ESPAÇAMENTOS ----------

data class Spacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    /** Margem lateral das telas. */
    val screen: Dp = 16.dp,
)

internal val AppSpacing = Spacing()

// ---------- WIDGET ----------

/** Tamanhos específicos do widget de tela inicial. */
internal object WidgetTokens {
    val padding = 12.dp
    val cornerRadius = 16.dp
    val itemSpacing = 4.dp
}
