package com.miojo.cerebelo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

/**
 * TEMPORÁRIO: mantém as telas antigas compilando durante a troca de visual.
 * Apagar quando todas as telas usarem Ink.spacing.
 */
data class LegacySpacing(
    val xxs: androidx.compose.ui.unit.Dp = 2.dp,
    val xs: androidx.compose.ui.unit.Dp = 4.dp,
    val sm: androidx.compose.ui.unit.Dp = 8.dp,
    val md: androidx.compose.ui.unit.Dp = 12.dp,
    val lg: androidx.compose.ui.unit.Dp = 16.dp,
    val xl: androidx.compose.ui.unit.Dp = 24.dp,
    val screen: androidx.compose.ui.unit.Dp = 20.dp,
)

private val legacy = LegacySpacing()

val MaterialTheme.spacing: LegacySpacing
    @Composable @ReadOnlyComposable get() = legacy
