package com.godzuche.bluechat.chat.domain

interface BluetoothSerializer<T> {
    fun encode(value: T): ByteArray
    fun decode(value: ByteArray, numByte: Int): T
}