package com.godzuche.bluechat.chat.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothController {
    val isConnected: StateFlow<Boolean>
    val pairedDevices: StateFlow<Set<BluetoothDeviceDomain>>
    val scannedDevices: StateFlow<Set<BluetoothDeviceDomain>>
    val error: SharedFlow<String>
    val isDiscovering: StateFlow<Boolean>
    val isDiscoveringFinished: StateFlow<Boolean>

    fun startDiscovery()
    fun stopDiscovery()
    fun updatePairedDevices()
    fun startBluetoothServer(): Flow<ConnectionResult>
    fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult>
    fun closeConnection()
    fun release()
    suspend fun trySendMessage(message: String): BluetoothMessage?
}