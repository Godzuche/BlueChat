package com.example.bluechat.chat.domain

data class BluetoothMessage(
    val message: String,
    val senderName: String,
    val isFromLocalUser: Boolean,
)