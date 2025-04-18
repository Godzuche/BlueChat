package com.godzuche.bluechat.chat.presentation

import com.godzuche.bluechat.chat.domain.BluetoothDevice
import com.godzuche.bluechat.chat.domain.BluetoothMessage

data class BluetoothUiState(
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isWaiting: Boolean = false,
    val errorMessage: String? = null,
    val isDiscovering: Boolean = false,
    val isDiscoveringFinished: Boolean = false,
    val messageInput: String = "",
    val messages: List<BluetoothMessage> = emptyList(),
)
