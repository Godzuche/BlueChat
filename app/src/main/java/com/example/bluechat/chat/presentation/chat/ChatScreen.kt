package com.example.bluechat.chat.presentation.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bluechat.R
import com.example.bluechat.chat.presentation.BluetoothUiState
import com.example.bluechat.chat.presentation.BluetoothViewModel
import com.example.bluechat.design_system.theme.BlueChatTheme

@Composable
fun ChatRoute(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
) {
    val uiState by bluetoothViewModel.state.collectAsState()

    ChatScreen(
        state = uiState,
//        onDisconnect = {},
        onSendMessage = { message ->
            //
        }
    )
}

@Composable
fun ChatScreen(
    state: BluetoothUiState,
//    onDisconnect: () -> Unit,
    onSendMessage: (String) -> Unit,
) {
    var message by remember {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(items = state.messages) { message ->
                Column {
                    ChatMessage(
                        message = message,
                        modifier = Modifier
                            .align(if (message.isFromLocalUser) Alignment.End else Alignment.Start)
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
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(text = stringResource(id = R.string.message))
                },
            )
            IconButton(onClick = { onSendMessage(message) }) {
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
fun ChatScreenPreview() {
    BlueChatTheme {
        ChatScreen(state = BluetoothUiState(), onSendMessage = {})
    }
}