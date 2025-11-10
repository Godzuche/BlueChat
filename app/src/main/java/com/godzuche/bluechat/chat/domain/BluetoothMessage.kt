package com.godzuche.bluechat.chat.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BluetoothMessage(
    val message: String,
    val senderName: String,
    val isFromLocalUser: Boolean,
)

@Serializable
sealed class BluetoothEvent {
    @Serializable
    @SerialName("message")
    data class Message(
        val message: String,
        val senderName: String,
        val isFromLocalUser: Boolean
    ) : BluetoothEvent()

    @Serializable
    @SerialName("typing")
    data class Typing(
        val senderName: String,
        val isTyping: Boolean    ) : BluetoothEvent()

//    @Serializable
//    @SerialName("status")
//    data class ConnectionStatus(
//        val senderName: String,
//        val connected: Boolean
//    ) : BluetoothEvent()
}
