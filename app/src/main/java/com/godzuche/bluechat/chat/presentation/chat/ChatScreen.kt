package com.godzuche.bluechat.chat.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bluechat.R
import com.godzuche.bluechat.chat.domain.BluetoothMessage
import com.godzuche.bluechat.chat.presentation.BluetoothUiState
import com.godzuche.bluechat.chat.presentation.BluetoothViewModel
import com.godzuche.bluechat.core.design_system.theme.BlueChatTheme

@Composable
fun ChatRoute(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
) {
    val uiState by bluetoothViewModel.state.collectAsStateWithLifecycle()

    ChatScreen(
        state = uiState,
        onMessageInputChange = bluetoothViewModel::onMessageInputChange,
        onSendMessage = bluetoothViewModel::sendMessage,
    )
}

@Composable
fun ChatScreen(
    state: BluetoothUiState,
    onMessageInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(items = state.messages) { message ->
                val alignment = if (message.isFromLocalUser) {
                    Alignment.End
                } else Alignment.Start

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ChatMessage(
                        message = message,
                        modifier = Modifier
                            .widthIn(max = screenWidth * 0.8f)// Limit max width to 80% of screen
                            .align(alignment)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = state.messageInput,
                onValueChange = onMessageInputChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(text = stringResource(id = R.string.message))
                },
            )
            IconButton(
                onClick = onSendMessage,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.secondary,
                    disabledContentColor = MaterialTheme.colorScheme.outline,
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send,
                    contentDescription = stringResource(id = R.string.send_message),
                    modifier = Modifier.size(56.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ChatScreenPreview() {
    BlueChatTheme {
        ChatScreen(
            state = BluetoothUiState()
                .copy(messages = previewMessages),
            onMessageInputChange = {},
            onSendMessage = {},
        )
    }
}

private val previewMessages = listOf(
    BluetoothMessage(
        message = "Hi",
        senderName = "John",
        isFromLocalUser = false,
    ),
    BluetoothMessage(
        message = "Good morning. How are you doing?",
        senderName = "John",
        isFromLocalUser = false,
    ),
    BluetoothMessage(
        message = "Hey! I'm doing great, thanks for asking! What about you?",
        senderName = "Doe",
        isFromLocalUser = true,
    ),
)