package com.godzuche.bluechat.chat.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.godzuche.bluechat.chat.domain.BluetoothController
import com.godzuche.bluechat.chat.domain.BluetoothDevice
import com.godzuche.bluechat.chat.domain.ConnectionResult
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

    fun initData() {
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
            Log.d("BT", "error: $error")
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
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .startBluetoothServer()
            .listen()
    }

    fun connectToDevice(device: BluetoothDevice) {
        stopScan()

        Log.d("Connect", "vm")
        _state.update { it.copy(isConnecting = true) }
        deviceConnectionJob = bluetoothController
            .connectToDevice(device)
            .listen()
    }

    fun disconnectFromDevice() {
        deviceConnectionJob?.cancel()
        bluetoothController.closeConnection()
        _state.update {
            it.copy(isConnecting = false, isConnected = false)
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
            val message = state.value.messageInput.trim()
            val sentMessage = bluetoothController.trySendMessage(message)
            Log.d("Chat", "bluetoothMessage vm: $sentMessage")
            if (sentMessage != null) {
                _state.update {
                    it.copy(
                        messages = it.messages + sentMessage,
                        messageInput = "",
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
                            errorMessage = null
                        )
                    }
                }

                is ConnectionResult.Error -> {
                    _state.update {
                        it.copy(
                            isConnected = false,
                            isConnecting = false,
                            errorMessage = result.errorMessage
                        )
                    }
                }

                is ConnectionResult.TransferSucceeded -> {
                    Log.d("Chat", "Received: ${result.message}")
                    _state.update {
                        val messages = it.messages + result.message
                        it.copy(
                            messages = messages
                        )
                    }
                    Log.d("Chat", "messages: ${state.value.messages}")
                }

            }
        }
            .catch { throwable ->
                bluetoothController.closeConnection()
                _state.update {
                    it.copy(
                        isConnected = false,
                        isConnecting = false,
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