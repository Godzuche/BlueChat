package com.example.bluechat.domain

import java.util.UUID

typealias BluetoothDeviceDomain = BluetoothDevice

/**
 * Use the type alias: [BluetoothDeviceDomain] to distinguish
 * this class from the Android Framework Bluetooth class
 * */
data class BluetoothDevice(
    val uuid: UUID?,
    val name: String?,
    val hardwareAddress: String,
)
