package com.godzuche.bluechat.chat.data.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import com.godzuche.bluechat.ALL_BT_PERMISSIONS
import com.godzuche.bluechat.chat.data.mappers.toBluetoothDeviceDomain
import com.godzuche.bluechat.chat.data.mappers.toBluetoothMessage
import com.godzuche.bluechat.chat.data.mappers.toByteArray
import com.godzuche.bluechat.chat.domain.BluetoothController
import com.godzuche.bluechat.chat.domain.BluetoothDeviceDomain
import com.godzuche.bluechat.chat.domain.BluetoothMessage
import com.godzuche.bluechat.chat.domain.ConnectionResult
import com.godzuche.bluechat.core.data.util.hasPermission
import com.godzuche.bluechat.core.data.util.haveAllPermissions
import com.godzuche.bluechat.core.presentation.util.debugLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
                debugLog { "BTT Found Device Domain: $newFoundDevice" }
                debugLog { "BTT Scanned Devices Domain: $scannedDevices" }
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
            when (isConnected) {
                true -> {
                    if (bluetoothAdapter?.bondedDevices?.contains(bluetoothDevice) == true) {
                        _isConnected.update { true }
                    } else {
                        CoroutineScope(Dispatchers.IO).launch {
                            _error.tryEmit("Can't connect to non-paired device")
                        }
                    }
                }

                false -> {
                    _isConnected.update { false }
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
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED) // For bluetooth switch state changes
                addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        )
        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            },
        )
    }

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context.hasPermission(Manifest.permission.BLUETOOTH_SCAN)
                .not()
        ) {
            return
        }

        _scannedDevices.update {
            emptySet()
        }

//        context.registerReceiver(
//            foundDeviceReceiver,
//            IntentFilter().apply {
//                addAction(BluetoothDevice.ACTION_FOUND)
//                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
//                addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
//            },
//        )

        updatePairedDevices()

//        if (bluetoothAdapter?.isDiscovering == true) {
        stopDiscovery()
//        }

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

        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter?.cancelDiscovery()
        }
    }

//    private fun startDiscovery2() {
//        if (checkPermissions()) {
//            if (bluetoothAdapter?.isDiscovering == false) {
//                bluetoothAdapter?.startDiscovery()
//            }
//        } else {
//            requestPermissions()
//        }
//    }

    @SuppressLint("MissingPermission")
    override fun startBluetoothServer(): Flow<ConnectionResult> {
        return flow {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && context.hasPermission(Manifest.permission.BLUETOOTH_CONNECT)
                    .not()
            ) {
                throw SecurityException("No BLUETOOTH_CONNECT permission")
            }

            currentServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                "bluechat_service",
                UUID.fromString(SERVICE_UUID)
            )

            var shouldLoop = true
            while (shouldLoop) {
                currentClientSocket = try {
                    currentServerSocket?.accept()
                } catch (e: IOException) {
                    emit(ConnectionResult.Error(e.localizedMessage ?: "Unknown error"))
                    shouldLoop = false
                    null
                }

//                emit(ConnectionResult.ConnectionEstablished)

                currentClientSocket?.let {
                    emit(ConnectionResult.ConnectionEstablished)
                    currentServerSocket?.close()
                    val service = BluetoothDataTransferService(it)
                    dataTransferService = service

                    emitAll(
                        service
                            .listenForIncomingData()
                            .map { data ->
                                ConnectionResult.TransferSucceeded(
                                    data.toBluetoothMessage(isFromLocalUser = false)
                                )
                            }
                    )
                    shouldLoop = false
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
            debugLog { "Connect uuid: ${device.uuid}" }
            currentClientSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(
                UUID.fromString(SERVICE_UUID)
            )
            debugLog {
                "Connect" +
                        if (currentClientSocket == null) "cl socket null" else "cl socket not null"
            }

            stopDiscovery()

            currentClientSocket?.let { socket ->
                try {
                    socket.connect()
                    debugLog { "Connect connection established" }
                    emit(ConnectionResult.ConnectionEstablished)
                    BluetoothDataTransferService(socket).also {
                        dataTransferService = it
                        emitAll(
                            it.listenForIncomingData().map { data ->
                                ConnectionResult.TransferSucceeded(
                                    data.toBluetoothMessage(isFromLocalUser = false)
                                )
                            })
                    }
                } catch (e: IOException) {
                    socket.close()
                    currentClientSocket = null
                    e.printStackTrace()
                    debugLog { "Connect connection error: ${e.localizedMessage}" }
                    emit(ConnectionResult.Error("Connection was interrupted \n " + e.localizedMessage))
                }
            }
        }
            .flowOn(Dispatchers.IO)
    }

    override fun closeConnection() {
        try {
            currentClientSocket?.close()
//            currentClientSocket = null
            currentServerSocket?.close()
//            currentServerSocket = null
        } catch (e: IOException) {
            debugLog { "BTT Could not close the connect socket" }
        }
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
            senderName = bluetoothAdapter?.name ?: "Unknown Name",
            isFromLocalUser = true,
        )

        val result = dataTransferService?.sendMessage(bluetoothMessage.toByteArray())
        debugLog { "Chat trySendMessage result: $result" }

        return bluetoothMessage
    }

    companion object {
        const val SERVICE_UUID = "27b7d1da-08c7-4505-a6d1-2459987e5e2d"
        const val SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }


    @SuppressLint("MissingPermission")
    private fun pairDevice(device: BluetoothDevice) {
        if (context.haveAllPermissions(ALL_BT_PERMISSIONS)) {
            try {
                val method = device.javaClass.getMethod("createBond")
                method.invoke(device)
                debugLog { "BluetoothPairing Pairing initiated with ${device.name}" }
            } catch (e: Exception) {
                Log.e("BluetoothPairing", "Error initiating pairing", e)
            }
        } else {
//        requestPermissions()
        }
    }

    private val pairingReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    when (device?.bondState) {
                        BluetoothDevice.BOND_BONDED -> debugLog { "Pairing Device paired: ${device.name}" }
                        BluetoothDevice.BOND_BONDING -> debugLog { "Pairing Pairing in progress: ${device.name}" }
                        BluetoothDevice.BOND_NONE -> debugLog { "Pairing Pairing failed or unpaired: ${device.name}" }
                    }
                }
            }
        }
    }

// Register and unregister the receiver
//private fun registerPairingReceiver() {
//    val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
//    context.registerReceiver(pairingReceiver, filter)
//}

    private val bluetoothDiscoverabilityStateReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    BluetoothAdapter.ACTION_SCAN_MODE_CHANGED -> {
                        val mode =
                            intent.getIntExtra(
                                BluetoothAdapter.EXTRA_SCAN_MODE,
                                BluetoothAdapter.ERROR
                            )
                        when (mode) {
                            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE -> {
                                debugLog { "Bluetooth Device is discoverable." }
                            }

                            BluetoothAdapter.SCAN_MODE_CONNECTABLE -> {
                                debugLog { "Bluetooth Device is connectable but not discoverable." }
                            }

                            BluetoothAdapter.SCAN_MODE_NONE -> {
                                debugLog { "Bluetooth Device is neither connectable nor discoverable." }
                            }
                        }
                    }
                }
            }
        }

    private fun registerBluetoothStateReceiver() {
        val filter =
            IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        context.registerReceiver(
            bluetoothDiscoverabilityStateReceiver,
            filter
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchDeviceUuids(device: BluetoothDevice) {
        device.fetchUuidsWithSdp() // Initiates the UUID fetch

        val uuidReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                if (BluetoothDevice.ACTION_UUID == intent.action) {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE
                        )
                    val uuids: Array<ParcelUuid>? =
                        intent.getParcelableArrayExtra(
                            BluetoothDevice.EXTRA_UUID
                        ) as? Array<ParcelUuid>
                    uuids?.forEach { uuid ->
                        debugLog { "BluetoothUUID Device: ${device?.name}, UUID: ${uuid.uuid}" }
                    }
                }
            }
        }

        // Register the receiver
        val filter =
            IntentFilter(BluetoothDevice.ACTION_UUID)
        context.registerReceiver(uuidReceiver, filter)
    }

}

/**
 * Immediate bluetooth connection check
 * to determine if a device is connected
 * */
private fun isBluetoothConnected(context: Context): Boolean {
    val bluetoothManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }
    if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
        return false // Bluetooth is off or not supported
    }

    // Check connected devices for specific profiles
    val profiles = listOf(
        BluetoothProfile.HEADSET,
        BluetoothProfile.A2DP,
        BluetoothProfile.GATT
    )

    for (profile in profiles) {
        val connectedDevices =
            context.getConnectedDevicesForProfile(
                profile
            )
        if (connectedDevices.isNotEmpty()) {
            return true // At least one device is connected
        }
    }

    return false // No devices connected
}

private fun Context.getConnectedDevicesForProfile(
    profile: Int,
): List<BluetoothDevice> {
    val bluetoothAdapter =
        BluetoothAdapter.getDefaultAdapter()
    val connectedDevices =
        mutableListOf<BluetoothDevice>()

    val serviceListener =
        object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(
                profile: Int,
                proxy: BluetoothProfile,
            ) {
                connectedDevices.addAll(proxy.connectedDevices)
                bluetoothAdapter.closeProfileProxy(
                    profile,
                    proxy
                )
            }

            override fun onServiceDisconnected(profile: Int) {
                // Handle disconnection if needed
            }
        }

    bluetoothAdapter.getProfileProxy(
        this,
        serviceListener,
        profile
    )
    return connectedDevices
}
