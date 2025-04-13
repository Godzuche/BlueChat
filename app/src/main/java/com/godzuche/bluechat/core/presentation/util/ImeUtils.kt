package com.godzuche.bluechat.core.presentation.util

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp

@Composable
fun customImePadding(
    additionalPadding: Dp = 0.dp,
): State<Dp> {
    val insets = WindowInsets.ime
    val density = LocalDensity.current
    return remember {
        derivedStateOf {
            with(density) {
                (insets.getBottom(density).toDp() + additionalPadding)
                    .coerceAtLeast(0.dp)
            }
        }
    }
}

@Composable
fun isKeyboardVisible(): State<Boolean> {
    val insets = WindowInsets.ime
    val density = LocalDensity.current
    return remember {
        derivedStateOf {
            insets.getBottom(density) > 0
        }
    }
}
