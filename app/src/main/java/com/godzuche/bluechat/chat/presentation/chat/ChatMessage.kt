package com.godzuche.bluechat.chat.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluechat.R
import com.godzuche.bluechat.chat.domain.BluetoothMessage
import com.godzuche.bluechat.core.design_system.theme.BlueChatTheme

@Composable
fun ChatMessage(
    message: BluetoothMessage,
    modifier: Modifier = Modifier,
    isFirstInARow: Boolean = true,
    isLastInARow: Boolean = true,
    isGroupMessage: Boolean = false, // Will be used for group chat messages and as a flag for sender name
) {
    val cornerRadius = 24.dp
    val topStart = when {
        isFirstInARow && isLastInARow -> cornerRadius
        isFirstInARow -> cornerRadius
        else -> 4.dp
    }
    val topEnd = when {
        isFirstInARow && isLastInARow -> cornerRadius
        isFirstInARow -> cornerRadius
        else -> 4.dp
    }
    val bottomStart = when {
        isLastInARow -> cornerRadius
        else -> 4.dp
    }
    val bottomEnd = when {
        isLastInARow -> cornerRadius
        else -> 4.dp
    }

    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (message.isFromLocalUser) cornerRadius else topStart,
                    topEnd = if (message.isFromLocalUser) topEnd else cornerRadius,
                    bottomStart = if (message.isFromLocalUser) cornerRadius else bottomStart,
                    bottomEnd = if (message.isFromLocalUser) bottomEnd else cornerRadius,
                )
            )
            .background(
                if (message.isFromLocalUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else MaterialTheme.colorScheme.surfaceContainerHighest
            )
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
    ) {
        Text(
            text = message.senderName,
            fontSize = 10.sp,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.W800,
            ),
//            color = if (message.isFromLocalUser) {
//                MaterialTheme.colorScheme.onPrimaryContainer
//            } else MaterialTheme.colorScheme.onSurface,
            color = if (message.isFromLocalUser) {
                MaterialTheme.colorScheme.secondary
            } else MaterialTheme.colorScheme.tertiary,
        )

        Text(
            text = message.message,
            modifier = Modifier.wrapContentWidth(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (message.isFromLocalUser) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@PreviewLightDark
@Composable
fun MessagePreview() {
    BlueChatTheme {
        Row {
            ChatMessage(
                message = BluetoothMessage(
                    message = stringResource(id = R.string.hello_world),
                    senderName = stringResource(id = R.string.samsung),
                    isFromLocalUser = false,
                )
            )
            ChatMessage(
                message = BluetoothMessage(
                    message = stringResource(id = R.string.hello_world),
                    senderName = stringResource(id = R.string.samsung),
                    isFromLocalUser = true,
                )
            )
        }
    }
}