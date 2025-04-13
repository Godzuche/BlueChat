package com.godzuche.bluechat.core.design_system.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Color.Transparent,
    ) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}