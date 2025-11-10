package com.godzuche.bluechat.chat.presentation

import com.godzuche.bluechat.chat.domain.BluetoothDevice
import com.godzuche.bluechat.chat.domain.BluetoothEvent

data class BluetoothUiState(
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false, // Connecting to host server
    val isWaitingForConnection: Boolean = false, // As a host, waiting for incoming connection
    val connectingStatusMessage: String? = null, // Status message when trying to establish a connection
    val connectionStatus: ConnectionStatus = ConnectionStatus.IDLE,
    val errorMessage: String? = null,
    val isDiscovering: Boolean = false,
    val isDiscoveringFinished: Boolean = false,
    val messageInput: String = "",
//    val messages: List<BluetoothMessage> = emptyList(),
    val messages: List<BluetoothEvent.Message> = emptyList(),
    val isTyping: BluetoothEvent.Typing? = null,
)
