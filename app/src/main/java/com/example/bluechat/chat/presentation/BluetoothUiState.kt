package com.example.bluechat.chat.presentation

import com.example.bluechat.chat.domain.BluetoothDevice
import com.example.bluechat.chat.domain.BluetoothMessage

data class BluetoothUiState(
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val isDiscovering: Boolean = false,
    val isDiscoveringFinished: Boolean = false,
    val messages: List<BluetoothMessage> = emptyList(),
)
