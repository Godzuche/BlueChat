package com.example.bluechat.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluechat.domain.BluetoothController
import com.example.bluechat.domain.BluetoothDevice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BluetoothViewModel @Inject constructor(
    private val bluetoothController: BluetoothController,
) : ViewModel() {

    private val _state = MutableStateFlow(BluetoothUiState())
    val state: StateFlow<BluetoothUiState> = combine(
        bluetoothController.pairedDevices,
        bluetoothController.scannedDevices,
        _state
    ) { pairedDevices, scannedDevices, state ->
        state.copy(
            pairedDevices = pairedDevices,
            scannedDevices = scannedDevices
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = _state.value
    )

    init {
        bluetoothController.isConnected.onEach { isConnected ->
            _state.update {
                it.copy(isConnected = isConnected)
            }
        }.launchIn(viewModelScope)

        bluetoothController.error.onEach { error ->
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
    }

    fun startScan() {
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        bluetoothController.stopDiscovery()
    }

    fun listenAndWaitForIncommingConnections() {
        //
    }

    fun connectToDevice(device: BluetoothDevice) {
        //
    }

    fun updatePairedDevices() {
        bluetoothController.updatePairedDevices()
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothController.release()
    }
}