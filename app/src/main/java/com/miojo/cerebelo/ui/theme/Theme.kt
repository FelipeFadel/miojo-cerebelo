package com.miojo.cerebelo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LocalInkColors = staticCompositionLocalOf { LightInk }
private val LocalInkType = staticCompositionLocalOf { AppInkType }
private val LocalInkSpacing = staticCompositionLocalOf { AppInkSpacing }

/** Acesso aos tokens: `Ink.colors.seal`, `Ink.type.body`, `Ink.spacing.screen`. */
object Ink {
    val colors: InkColors
        @Composable @ReadOnlyComposable get() = LocalInkColors.current
    val type: InkType
        @Composable @ReadOnlyComposable get() = LocalInkType.current
    val spacing: InkSpacing
        @Composable @ReadOnlyComposable get() = LocalInkSpacing.current
}

/** Aplica os tokens de DesignTokens.kt. Não coloque valores visuais aqui. */
@Composable
fun CerebeloTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val ink = if (darkTheme) DarkInk else LightInk
    CompositionLocalProvider(
        LocalInkColors provides ink,
        LocalInkType provides AppInkType,
        LocalInkSpacing provides AppInkSpacing,
        LocalTextSelectionColors provides TextSelectionColors(
            handleColor = ink.seal,
            backgroundColor = ink.seal.copy(alpha = 0.25f),
        ),
    ) {
        // Material só como base (cursor, diálogos do sistema); o visual vem dos componentes Ink*.
        MaterialTheme(colorScheme = materialColors(ink, darkTheme), content = content)
    }
}

/** Esquema de cores Material derivado dos tokens (usado também pelo widget). */
internal fun inkMaterialColors(dark: Boolean) = materialColors(if (dark) DarkInk else LightInk, dark)

private fun materialColors(ink: InkColors, dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = ink.ink, onPrimary = ink.page,
        secondary = ink.person, tertiary = ink.seal,
        background = ink.page, onBackground = ink.ink,
        surface = ink.page, onSurface = ink.ink,
        surfaceVariant = ink.page, onSurfaceVariant = ink.inkSoft,
        surfaceContainer = ink.page, surfaceContainerLow = ink.page, surfaceContainerHigh = ink.page,
        surfaceContainerHighest = ink.page, surfaceContainerLowest = ink.page,
        outline = ink.stroke, error = ink.seal, surfaceTint = Color.Transparent,
    )
} else {
    lightColorScheme(
        primary = ink.ink, onPrimary = ink.page,
        secondary = ink.person, tertiary = ink.seal,
        background = ink.page, onBackground = ink.ink,
        surface = ink.page, onSurface = ink.ink,
        surfaceVariant = ink.page, onSurfaceVariant = ink.inkSoft,
        surfaceContainer = ink.page, surfaceContainerLow = ink.page, surfaceContainerHigh = ink.page,
        surfaceContainerHighest = ink.page, surfaceContainerLowest = ink.page,
        outline = ink.stroke, error = ink.seal, surfaceTint = Color.Transparent,
    )
}
