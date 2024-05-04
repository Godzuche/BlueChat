package com.example.bluechat.chat.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.bluechat.chat.domain.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain() = BluetoothDeviceDomain(
    uuid = this.uuids?.first()?.uuid,
    name = name,
    hardwareAddress = address
)