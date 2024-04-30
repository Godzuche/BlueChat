package com.example.bluechat.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import com.example.bluechat.data.util.hasPermission
import com.example.bluechat.domain.BluetoothController
import com.example.bluechat.domain.BluetoothDeviceDomain
import com.example.bluechat.domain.BluetoothMessage
import com.example.bluechat.domain.ConnectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
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

    private var dataTransferService: BluetoothDataTransferService? = null

    @SuppressLint("MissingPermission")
    private val foundDeviceReceiver = FoundDeviceReceiver(
        onDeviceFound = { foundDevice ->
            _scannedDevices.update { scannedDevices ->
                val newFoundDevice = foundDevice.toBluetoothDeviceDomain()
                scannedDevices + newFoundDevice
            }
        },
        onDiscoveryFinished = { isFinished ->
            _isDiscoveringFinished.update { isFinished }
            _isDiscovering.update { bluetoothAdapter?.isDiscovering == true && isFinished.not() }
        }
    )

    @SuppressLint("MissingPermission")
    private val bluetoothStateReceiver = BluetoothStateReceiver(
        onStateChange = { isConnected, bluetoothDevice ->
            if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
                _isConnected.update { isConnected }
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    _error.tryEmit("Can't connect to non-paired device")
                }
            }
        }
    )

    private var currentClientSocket: BluetoothSocket? = null
    private var currentServerSocket: BluetoothServerSocket? = null

    init {
        updatePairedDevices()
        context.registerReceiver(
            bluetoothStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
    }

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.hasPermission(Manifest.permission.BLUETOOTH_SCAN).not()) {
            return
        }

        _scannedDevices.update {
            emptySet()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
                .not()
        ) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.hasPermission(Manifest.permission.BLUETOOTH_SCAN)
                .not()
        ) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
    }

    @SuppressLint("MissingPermission")
    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    .not()
            ) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentServerSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                "chat service",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch (e: IOException) {
                    shouldLoop = false
                    null
                }

                emit(ConnectionResult.ConnectionEstablished)

                currentClientSocket?.let {
                    currentServerSocket?.close()
                    val service = BluetoothDataTransferService(it)
                    dataTransferService = service

                    emitAll(
                        service
                            .listenForIncommingData()
                            .map {
                                ConnectionResult.TransferSucceeded(it)
                            }
                    )
                }
            }

        }.flowOn(Dispatchers.IO)
    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: BluetoothDeviceDomain): Flow<ConnectionResult> {
        return flow {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    .not()
            ) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            val bluetoothDevice by lazy {
                bluetoothAdapter?.getRemoteDevice(device.hardwareAddress)
            }
            currentClientSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(
                UUID.fromString(SERVICE_UUID)
            )

            stopDiscovery()

            currentClientSocket?.let { socket ->
                try {
                    CoroutineScope(Dispatchers.IO).async {
                        socket.connect()
                    }.await()
                    emit(ConnectionResult.ConnectionEstablished)
                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(it.listenForIncommingData().map { data ->
                            ConnectionResult.TransferSucceeded(data)
                        })
                    }
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    e.printStackTrace()
                    emit(ConnectionResult.Error("Connection was interrupted \n " + e.localizedMessage))
                }
            }
        }
            .flowOn(Dispatchers.IO)
    }

    override fun closeConnection() {
        currentClientSocket?.close()
        currentClientSocket = null
        currentServerSocket?.close()
        currentServerSocket = null
    }

    override fun release() {
        context.unregisterReceiver(foundDeviceReceiver)
        context.unregisterReceiver(bluetoothStateReceiver)
        closeConnection()
    }

    @SuppressLint("MissingPermission")
    override suspend fun trySendMessage(message: String): BluetoothMessage? {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
                .not()
        ) {
            return null
        }

        if (dataTransferService == null) {
            return null
        }

        val bluetoothMessage = BluetoothMessage(
            message = message,
            senderName = bluetoothAdapter?.name ?: "Unknown name",
            isFromLocalUser = true
        )

        dataTransferService?.sendMessage(message.toByteArray())

        return bluetoothMessage
    }

    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
    }
}