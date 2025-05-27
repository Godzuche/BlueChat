package com.godzuche.bluechat.chat.presentation.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bluechat.R
import com.godzuche.bluechat.chat.domain.BluetoothMessage
import com.godzuche.bluechat.chat.presentation.BluetoothUiState
import com.godzuche.bluechat.chat.presentation.BluetoothViewModel
import com.godzuche.bluechat.core.design_system.components.MessageField
import com.godzuche.bluechat.core.design_system.theme.BlueChatTheme
import com.godzuche.bluechat.core.presentation.util.animateScrollToItem
import com.godzuche.bluechat.core.presentation.util.isKeyboardVisible
import com.godzuche.bluechat.core.presentation.util.lastVisibleItemIndex

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
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val listState = rememberLazyListState()
    val keyboardVisible by isKeyboardVisible()

    val scrollToBottom = remember(listState) {
        suspend {
            listState.animateScrollToItem(
                index = listState.layoutInfo.totalItemsCount - 1,
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
            )
        }
    }

    LaunchedEffect(keyboardVisible, state.messages) {
//        if (keyboardVisible) {
//            scrollToBottom()
//        }
        if (!listState.isScrollInProgress && listState.lastVisibleItemIndex < state.messages.lastIndex) {
            scrollToBottom()
        }
    }

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding()
            .fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(items = state.messages) { index, message ->
                val alignment = if (message.isFromLocalUser) {
                    Alignment.End
                } else Alignment.Start

                val previousMessage = state.messages.getOrNull(index - 1)
                val nextMessage = state.messages.getOrNull(index + 1)
                val isFirstInARow =
                    previousMessage == null || message.senderName != previousMessage.senderName
                val isLastInARow =
                    nextMessage == null || message.senderName != nextMessage.senderName

                Column(modifier = Modifier.fillMaxWidth()) {
                    ChatMessage(
                        isFirstInARow = isFirstInARow,
                        isLastInARow = isLastInARow,
                        message = message,
                        modifier = Modifier
                            .widthIn(max = screenWidth * 0.85f) // Limit max width to 80% of screen
                            .align(alignment)
                            .padding(horizontal = 12.dp)
                            .then(
                                previousMessage?.let {
                                    if (it.senderName != message.senderName) {
                                        Modifier.padding(top = 16.dp)
                                    } else {
                                        Modifier.padding(top = 4.dp)
                                    }
                                } ?: Modifier
                            ),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MessageField(
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .weight(1f),
                value = state.messageInput,
                singleLine = false,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.None,
                    autoCorrectEnabled = true,
                ),
                onValueChange = onMessageInputChange,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.message_label),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
            )

            FilledIconButton(
                onClick = onSendMessage,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(id = R.string.send_message),
                )
            }
        }
    }
}

@PreviewLightDark
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
        message = "Good morning. How are you doing?",
        senderName = "John",
        isFromLocalUser = false,
    ),
    BluetoothMessage(
        message = "Hey! I'm doing great, thanks for asking! What about you?",
        senderName = "Doe",
        isFromLocalUser = true,
    ),
    BluetoothMessage(
        message = "Hey! I'm doing great. :)",
        senderName = "Doe",
        isFromLocalUser = true,
    ),
    BluetoothMessage(
        message = "Hey! :)",
        senderName = "Doe",
        isFromLocalUser = true,
    ),
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