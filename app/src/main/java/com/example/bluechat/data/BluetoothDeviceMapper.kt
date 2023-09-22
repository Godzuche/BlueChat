package com.example.bluechat.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.example.bluechat.domain.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain() = BluetoothDeviceDomain(
    uuid = this.uuids?.first()?.uuid,
    name = name,
    hardwareAddress = address
)