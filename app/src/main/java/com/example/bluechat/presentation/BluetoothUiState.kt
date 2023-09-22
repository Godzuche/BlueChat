package com.example.bluechat.presentation

import com.example.bluechat.domain.BluetoothDevice

data class BluetoothUiState(
    val pairedDevices: Set<BluetoothDevice> = emptySet(),
    val scannedDevices: Set<BluetoothDevice> = emptySet(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val isDiscovering: Boolean = false,
    val isDiscoveringFinished: Boolean = false,
)
