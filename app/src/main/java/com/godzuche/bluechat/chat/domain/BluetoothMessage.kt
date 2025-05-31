package com.godzuche.bluechat.chat.domain

import kotlinx.serialization.Serializable

@Serializable
data class BluetoothMessage(
    val message: String,
    val senderName: String,
    val isFromLocalUser: Boolean,
)
