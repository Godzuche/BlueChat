package com.godzuche.bluechat.chat.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class FoundDeviceReceiver(
    private val onDeviceFound: (BluetoothDevice) -> Unit,
    private val onDiscoveryFinished: (Boolean) -> Unit,
) : BroadcastReceiver() {
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            BluetoothDevice.ACTION_FOUND -> {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }

                Log.d("BTT", "Found Device: ${device?.uuids}")

                device?.let(onDeviceFound)
            }

            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                onDiscoveryFinished(false)
            }

            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                onDiscoveryFinished(true)
            }
        }
    }
}