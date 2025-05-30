package com.godzuche.bluechat.chat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godzuche.bluechat.chat.domain.BluetoothController
import com.godzuche.bluechat.chat.domain.BluetoothDevice
import com.godzuche.bluechat.chat.domain.ConnectionResult
import com.godzuche.bluechat.core.presentation.util.debugLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
            _state.update {
                it.copy(pairedDevices = pairedDevices.toList())
            }
        }.launchIn(viewModelScope)

        bluetoothController.scannedDevices.onEach { scannedDevices ->
            _state.update {
                it.copy(scannedDevices = scannedDevices.toList())
            }
        }.launchIn(viewModelScope)

        bluetoothController.isConnected.onEach { isConnected ->
            _state.update {
                it.copy(
                    isConnected = isConnected,
//                    messages = if(isConnected) it.messages else emptyList(),
                )
            }
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
        _state.update {
            it.copy(
                isConnecting = false,
                isWaiting = true,
            )
        }
        deviceConnectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    fun stopListeningForIncomingConnections() {
        debugLog { "Stop listening for incoming connections" }
//        if (state.value.isConnecting) {
        if (state.value.isWaiting) {
            _state.update {
                it.copy(
//                    isConnecting = false,
                    isWaiting = false,
                )
            }
            deviceConnectionJob?.cancel()
            bluetoothController.closeConnection()
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        stopScan()

        debugLog { "Connect vm" }
        _state.update {
            it.copy(
                isConnecting = true,
                isWaiting = false,
            )
        }
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update {
            it.copy(
                isConnecting = false,
                isConnected = false,
                isWaiting = false,
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

    fun sendMessage() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val message = state.value.messageInput.trim()
                val sentMessage = bluetoothController.trySendMessage(message)
                debugLog { "Chat bluetoothMessage vm: $sentMessage" }
                if (sentMessage != null) {
                    _state.update {
                        it.copy(
                            messages = it.messages + sentMessage,
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
                            isConnected = true,
                            isConnecting = false,
                            isWaiting = false,
                            errorMessage = null,
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            isWaiting = false,
                            errorMessage = result.errorMessage,
                        )
                    }
                }

                is ConnectionResult.TransferSucceeded -> {
                    debugLog { "Chat Received: ${result.message}" }
                    _state.update {
                        val messages = it.messages + result.message
                        it.copy(messages = messages)
                    }
                    debugLog { "Chat messages: ${state.value.messages}" }
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
                        isWaiting = false,
                        errorMessage = throwable.localizedMessage ?: "Unknown error",
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