package com.godzuche.bluechat.chat.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluechat.R
import com.godzuche.bluechat.chat.domain.BluetoothMessage
import com.godzuche.bluechat.core.design_system.theme.BlueChatTheme

@Composable
fun ChatMessage(
    message: BluetoothMessage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = if (message.isFromLocalUser) 15.dp else 0.dp,
                    topEnd = 15.dp,
                    bottomStart = 15.dp,
                    bottomEnd = if (message.isFromLocalUser) 0.dp else 15.dp,
                )
            )
            .background(
                if (message.isFromLocalUser) {
                    MaterialTheme.colorScheme.primary
                } else MaterialTheme.colorScheme.surfaceContainer
            )
            .padding(12.dp),
    ) {
        Text(
            text = message.senderName,
            fontSize = 10.sp,
            style = MaterialTheme.typography.labelSmall,
            color = if (message.isFromLocalUser) {
                MaterialTheme.colorScheme.onPrimary
            } else MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = message.message,
            modifier = Modifier.widthIn(max = 250.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (message.isFromLocalUser) {
                MaterialTheme.colorScheme.onPrimary
            } else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview
@Composable
fun MessagePreview() {
    BlueChatTheme {
        ChatMessage(
            message = BluetoothMessage(
                message = stringResource(id = R.string.hello_world),
                senderName = stringResource(id = R.string.samsung),
                isFromLocalUser = true,
            )
        )
    }
}