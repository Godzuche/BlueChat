package com.example.bluechat.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.example.bluechat.domain.BluetoothController
import com.example.bluechat.domain.BluetoothDeviceDomain
import com.example.bluechat.domain.BluetoothMessage
import com.example.bluechat.domain.ConnectionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class AndroidBluetoothController @Inject constructor(
    private val context: Context,
) : BluetoothController {

    private val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }

    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _pairedDevices = MutableStateFlow<Set<BluetoothDeviceDomain>>(emptySet())
    override val pairedDevices: StateFlow<Set<BluetoothDeviceDomain>>
        get() = _pairedDevices.asStateFlow()

    private val _scannedDevices = MutableStateFlow<Set<BluetoothDeviceDomain>>(emptySet())
    override val scannedDevices: StateFlow<Set<BluetoothDeviceDomain>>
        get() = _scannedDevices.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    override val error: SharedFlow<String>
        get() = _error.asSharedFlow()

    private val _isDiscovering = MutableStateFlow(false)
    override val isDiscovering: StateFlow<Boolean>
        get() = _isDiscovering.asStateFlow()

    private val _isDiscoveringFinished = MutableStateFlow(false)
    override val isDiscoveringFinished: StateFlow<Boolean>
        get() = _isDiscoveringFinished.asStateFlow()

    @SuppressLint("MissingPermission")
    private val foundDeviceReceiver = FoundDeviceReceiver(
        onDeviceFound = { foundDevice ->
            val newFoundDevice = foundDevice.toBluetoothDeviceDomain()
            _scannedDevices.update { scannedDevices ->
                scannedDevices + newFoundDevice
            }
        },
        onDiscoveryFinished = { isFinished ->
            _isDiscoveringFinished.update { isFinished }
            _isDiscovering.update { /*bluetoothAdapter?.isDiscovering == true &&*/ isFinished.not() }
        }
    )

    init {
        updatePairedDevices()
    }

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hasPermission(Manifest.permission.BLUETOOTH_SCAN).not()) {
            return
        }

        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            }
        )

        updatePairedDevices()

        if (bluetoothAdapter?.isDiscovering == true) {
            stopDiscovery()
        }

        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    override fun updatePairedDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hasPermission(Manifest.permission.BLUETOOTH_CONNECT).not()) {
            return
        }

        bluetoothAdapter
            ?.bondedDevices
            ?.map { it.toBluetoothDeviceDomain() }
            ?.also { devices ->
                _pairedDevices.update { devices.toSet() }
            }
    }

    @SuppressLint("MissingPermission")
    override fun stopDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hasPermission(Manifest.permission.BLUETOOTH_SCAN).not()) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            emit(ConnectionResult.ConnectionEstablished)
        }
    }

    override fun connectToDevice(): Flow<ConnectionResult> {
        return flow {
            emit(ConnectionResult.ConnectionEstablished)
        }
    }

    override fun closeConnection() {
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        closeConnection()
    }

    override suspend fun trySendMessage(message: String): BluetoothMessage {
        return BluetoothMessage("", "", true)
    }

    private fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }
}