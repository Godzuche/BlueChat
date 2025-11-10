package com.godzuche.bluechat.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godzuche.bluechat.chat.domain.BluetoothController
import com.godzuche.bluechat.chat.domain.BluetoothDevice
import com.godzuche.bluechat.chat.domain.BluetoothEvent
import com.godzuche.bluechat.chat.domain.ConnectionResult
import com.godzuche.bluechat.core.presentation.util.debugLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    val state: StateFlow<BluetoothUiState> = _state.asStateFlow()

    private var deviceConnectionJob: Job? = null

    init {
        initData()
    }

    private fun initData() {
//        viewModelScope.launch(Dispatchers.Default) {
        bluetoothController.pairedDevices.onEach { pairedDevices ->
//            debugLog { "BBBB Paired Devices: $pairedDevices" }
            _state.update {
                it.copy(pairedDevices = pairedDevices.toList())
            }
        }.launchIn(viewModelScope)

        bluetoothController.scannedDevices.onEach { scannedDevices ->
//            debugLog { "BBBB Scanned Devices: $scannedDevices" }
            _state.update {
                it.copy(scannedDevices = scannedDevices.toList())
            }
        }.launchIn(viewModelScope)

        bluetoothController.isConnected.onEach { isConnected ->
//            _state.update {
//                it.copy(
//                    isConnected = isConnected,
//                    connectionStatus = if (isConnected) {
//                        ConnectionStatus.CONNECTED
//                    } else ConnectionStatus.NOT_CONNECTED,
////                    messages = if(isConnected) it.messages else emptyList(),
//                )
//            }
        }.launchIn(viewModelScope)

        bluetoothController.error.onEach { error ->
            debugLog { "BT error: $error" }
            _state.update {
                it.copy(errorMessage = error)
            }
        }.launchIn(viewModelScope)

        bluetoothController.isDiscovering.onEach { isScanning ->
            _state.update {
                it.copy(isDiscovering = isScanning)
            }
        }.launchIn(viewModelScope)

        bluetoothController.isDiscoveringFinished.onEach { isFinished ->
            _state.update {
                it.copy(isDiscoveringFinished = isFinished)
            }
        }.launchIn(viewModelScope)
//        }
    }

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    fun listenAndWaitForIncomingConnections() {
        stopScan()

        _state.update {
            it.copy(
                isConnecting = false,
                isWaitingForConnection = true,
                connectionStatus = ConnectionStatus.WAITING_FOR_CONNECTION,
            )
        }
        deviceConnectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    fun stopListeningForIncomingConnections() {
        debugLog { "Stop listening for incoming connections" }
//        if (state.value.isConnecting) {
        if (state.value.isWaitingForConnection) {
            _state.update {
                it.copy(
//                    isConnecting = false,
                    isWaitingForConnection = false,
                )
            }
            deviceConnectionJob?.cancel()
            bluetoothController.closeConnection()
        }
    }

    fun onCancelConnection() {
        when (state.value.connectionStatus) {
            ConnectionStatus.CONNECTING_TO_DEVICE,
            ConnectionStatus.WAITING_FOR_CONNECTION -> {
                deviceConnectionJob?.cancel()
                bluetoothController.closeConnection()
                _state.update {
                    it.copy(
//                    isConnecting = false,
//                        isWaitingForConnection = false,
                        connectionStatus = ConnectionStatus.NOT_CONNECTED,
                    )
                }

                deviceConnectionJob?.cancel()
                bluetoothController.closeConnection()
            }

            else -> Unit
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        stopScan()

        debugLog { "Connect vm" }
        _state.update {
            it.copy(
                isConnecting = true,
                isWaitingForConnection = false,
                connectionStatus = ConnectionStatus.CONNECTING_TO_DEVICE,
            )
        }
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun stopConnectingToDevice() {
        debugLog { "Stop trying to establish a connection" }
        if (state.value.isConnecting) {
            _state.update {
                it.copy(
                    isConnecting = false,
//                    isWaitingForConnection = false,
                )
            }
            deviceConnectionJob?.cancel()
            bluetoothController.closeConnection()
        }
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update {
            it.copy(
                isConnecting = false,
                isConnected = false,
                isWaitingForConnection = false,
                connectionStatus = ConnectionStatus.DISCONNECTED,
                messages = emptyList(), // Clear on new connection until we use local storage for persistence
            )
        }
    }

    fun updatePairedDevices() {
        bluetoothController.updatePairedDevices()
    }

    fun onMessageInputChange(input: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(messageInput = input)
            }
        }
    }

//    fun sendMessage() {
//        viewModelScope.launch(Dispatchers.Default) {
//            try {
//                val message = state.value.messageInput.trim()
//                val sentBluetoothMessage = bluetoothController.trySendMessage(message)
//                debugLog { "Chat bluetoothMessage vm: $sentBluetoothMessage" }
//                if (sentBluetoothMessage != null) {
//                    _state.update {
//                        it.copy(
//                            messages = it.messages + sentBluetoothMessage,
//                            messageInput = "",
//                        )
//                    }
//                }
//            } catch (t: Throwable) {
//                t.printStackTrace()
//                _state.update {
//                    it.copy(
//                        errorMessage = t.localizedMessage,
//                    )
//                }
//            }
//        }
//    }


    fun sendMessage2() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val message = state.value.messageInput.trim()
                val sentBluetoothMessage = bluetoothController.trySendEvent(
                    BluetoothEvent.Message(
                        message = message,
                        senderName = "", // will add the sender name in the controller before sending
                        isFromLocalUser = true,
                    )
                )
                debugLog { "Chat bluetoothMessage vm: $sentBluetoothMessage" }

                if (sentBluetoothMessage != null) {
                    _state.update {
                        it.copy(
                            messages = it.messages + sentBluetoothMessage as BluetoothEvent.Message,
                            messageInput = "",
                        )
                    }
                }
            } catch (t: Throwable) {
                t.printStackTrace()
                _state.update {
                    it.copy(
                        errorMessage = t.localizedMessage,
                    )
                }
            }
        }
    }

    private fun Flow<ConnectionResult>.listen(): Job {
        return onEach { result ->

            when (result) {
                ConnectionResult.ConnectionEstablished -> {
                    _state.update {
                        it.copy(
                            connectionStatus = ConnectionStatus.INITIALIZING_CHAT
                        )
                    }

                    // Todo: fetch and initialize cached messages and any relevant data

                    delay(2000) // fake initialization

                    _state.update {
                        it.copy(
                            isConnected = true,
                            isConnecting = false,
                            isWaitingForConnection = false,
                            errorMessage = null,
                            connectionStatus = ConnectionStatus.CONNECTED,
                            messages = emptyList(), // Clear on new connection until storage cache
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            isWaitingForConnection = false,
                            errorMessage = result.errorMessage,
                            connectionStatus = ConnectionStatus.NOT_CONNECTED,
                            messages = emptyList(), // Clear on new connection until we use local storage for persistence
                        )
                    }
                }

                is ConnectionResult.TransferSucceeded -> {
//                    debugLog { "Chat Received: ${result.message}" }
//                    _state.update {
//                        val messages = it.messages + result.message
//                        it.copy(messages = messages)
//                    }
//                    debugLog { "Chat messages: ${state.value.messages}" }


                    debugLog { "Chat Received: ${result.message}" }
                    when (result.message) {
                        is BluetoothEvent.Message -> {
                            _state.update {
                                val messages = it.messages + result.message
                                it.copy(messages = messages)
                            }
                            debugLog { "Chat messages: ${state.value.messages}" }
                        }

                        is BluetoothEvent.Typing -> {
                            _state.update {
                                it.copy(isTyping = result.message)
                            }
//                            debugLog { "Chat messages: ${state.value.messages}" }
                        }

                        else -> Unit
                    }
                }

            }
        }
            .catch { throwable ->
                throwable.printStackTrace()
                debugLog { "Error VM: ${throwable.localizedMessage}" }
                bluetoothController.closeConnection()
                _state.update {
                    it.copy(
                        isConnected = false,
                        isConnecting = false,
                        isWaitingForConnection = false,
                        errorMessage = throwable.localizedMessage ?: "Unknown error",
                        connectionStatus = ConnectionStatus.NOT_CONNECTED,
                        messages = emptyList(), // Clear on new connection until we use local storage for persistence
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}

enum class ConnectionStatus {
    CONNECTING_TO_DEVICE,
    WAITING_FOR_CONNECTION,
    INITIALIZING_CHAT,
    CONNECTED,
    NOT_CONNECTED,
    DISCONNECTED,
//    CANCELED,
    IDLE,
}

//@Serializable(with = ChatEventSerializer::class)
//enum class ChatEvent {
//    TYPING,
//    STOP_TYPING;
//
//    companion object {
//        fun fromString(value: String?): ChatEvent {
//            require(!value.isNullOrBlank()) { "Chat event value cannot be null or blank" }
//
//            return ChatEvent.entries.firstOrNull {
//                it.name.equals(value.trim(), ignoreCase = true) ||
//                        it.name.equals(value.trim(), ignoreCase = true)
//            } ?: throw IllegalArgumentException("Invalid chat event value: '$value'")
//        }
//    }
//}
//
//object ChatEventSerializer : KSerializer<ChatEvent> {
//    override val descriptor = PrimitiveSerialDescriptor("ChatEvent", PrimitiveKind.STRING)
//
//    override fun serialize(encoder: Encoder, value: ChatEvent) {
//        encoder.encodeString(value.name)
//    }
//
//    override fun deserialize(decoder: Decoder): ChatEvent {
//        val raw = decoder.decodeString()
//        return ChatEvent.fromString(raw)
//    }
//}