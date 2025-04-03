package com.godzuche.bluechat.chat.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * Real-time update of bluetooth state used to react to bluetooth switch state changes
 * and connection state changes
 * */
class BluetoothStateReceiver(
    private val onStateChange: (isConnected: Boolean, BluetoothDevice) -> Unit,
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }

        Log.d("BTT", "Intent action: ${intent?.action}")
        when (intent?.action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                onStateChange(true, device ?: return)
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                onStateChange(false, device ?: return)
            }

            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                val isBluetoothEnabled = (state == BluetoothAdapter.STATE_ON)
                Log.d("BTT", "Bluetooth enabled: $isBluetoothEnabled")

                when (state) {
                    BluetoothAdapter.STATE_TURNING_ON -> {}
                    BluetoothAdapter.STATE_ON -> {}
                    BluetoothAdapter.STATE_TURNING_OFF -> {}
                    BluetoothAdapter.STATE_OFF -> {}
                }
            }
        }
    }
}