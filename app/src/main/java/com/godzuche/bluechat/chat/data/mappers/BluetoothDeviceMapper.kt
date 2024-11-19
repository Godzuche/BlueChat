package com.godzuche.bluechat.chat.data.mappers

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.godzuche.bluechat.chat.domain.BluetoothDeviceDomain

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBluetoothDeviceDomain() = BluetoothDeviceDomain(
    uuid = this.uuids?.first()?.uuid,
    name = name,
    hardwareAddress = address
)