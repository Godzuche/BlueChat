package com.godzuche.bluechat.chat.presentation.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.bluechat.R
import com.godzuche.bluechat.chat.domain.BluetoothMessage
import com.godzuche.bluechat.chat.presentation.BluetoothUiState
import com.godzuche.bluechat.chat.presentation.BluetoothViewModel
import com.godzuche.bluechat.core.design_system.theme.BlueChatTheme
import com.godzuche.bluechat.core.presentation.util.animateScrollToItem
import com.godzuche.bluechat.core.presentation.util.isKeyboardVisible

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

    LaunchedEffect(keyboardVisible) {
        if (keyboardVisible) {
            scrollToBottom()
        }
    }

    Column(
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
//                .imeNestedScroll()
                .weight(1f)
        ) {
            itemsIndexed(items = /*state.messages*/ previewMessages) { index, message ->
                val alignment = if (message.isFromLocalUser) {
                    Alignment.End
                } else Alignment.Start
                val previousMessage = previewMessages.getOrNull(index - 1)
                val nextMessage = previewMessages.getOrNull(index + 1)
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
//            TextField(
//                value = state.messageInput,
//                onValueChange = onMessageInputChange,
//                modifier = Modifier.weight(1f),
//                placeholder = {
//                    Text(text = stringResource(id = R.string.message))
//                },
//                colors = TextFieldDefaults.colors(
//                    disabledIndicatorColor = Color.Transparent,
//                    focusedIndicatorColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent,
//                    errorIndicatorColor = Color.Transparent,
//                ),
//            )
            // Todo: Make Multiline
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
                error = null,
                onValueChange = onMessageInputChange,
                placeholder = {
                    Text(
                        text = "Message",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                },
                leadingIcon = null,
            )

            FilledIconButton(
                onClick = onSendMessage,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send,
                    contentDescription = stringResource(id = R.string.send_message),
                )
            }
        }
    }
}

@Composable
private fun MessageField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
        .copy(color = MaterialTheme.colorScheme.onSurface),
    error: (@Composable () -> Unit)? = null,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        imeAction = ImeAction.None,
        autoCorrectEnabled = false,
    ),
    singleLine: Boolean = true,
    shape: Shape = RoundedCornerShape(24.dp),
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    val isHintVisible by remember(value) { mutableStateOf(value.isEmpty()) }
    val customTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.primary,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
    )

    Column(
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            BasicTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                },
                cursorBrush = SolidColor(
                    if (error != null) {
                        MaterialTheme.colorScheme.error
                    } else MaterialTheme.colorScheme.primary
                ),
                singleLine = singleLine,
                keyboardOptions = keyboardOptions,
                textStyle = textStyle,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(shape)
                    .background(containerColor),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = verticalAlignment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 8.dp
                            ),
                    ) {
                        if (leadingIcon != null) {
                            leadingIcon()
                            Spacer(modifier = Modifier.width(7.dp))
                        }
                        Box {
                            if (isHintVisible) {
                                placeholder?.invoke()
                            }
                            ProvideTextStyle(textStyle) {
                                innerTextField()
                            }
                        }
                        if (trailingIcon != null) {
                            if (value.isNotEmpty()) {
                                Spacer(modifier = Modifier.weight(1f))
                                Spacer(modifier = Modifier.width(6.dp))
                                trailingIcon()
                            }
                        }
                    }
                }
            )
        }

        error?.invoke()
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