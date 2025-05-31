package com.godzuche.bluechat.chat.data

import com.godzuche.bluechat.chat.domain.BluetoothMessage
import com.godzuche.bluechat.chat.domain.BluetoothSerializer
import com.godzuche.bluechat.core.presentation.util.debugLog
import kotlinx.serialization.json.Json

object BluetoothMessageSerializer : BluetoothSerializer<BluetoothMessage> {
    override fun encode(value: BluetoothMessage): ByteArray {
        val jsonString = Json.encodeToString(value)
        debugLog { "Encoded json: $jsonString" }
        val encodedBytes = jsonString.encodeToByteArray()
        debugLog { "Encoded bytes: $encodedBytes" }
        return encodedBytes
    }

    override fun decode(value: ByteArray, numBytes: Int): BluetoothMessage {
        val decodedJsonString = value.decodeToString(endIndex = numBytes)
        debugLog { "Decoded json: $decodedJsonString" }
        val message = Json.decodeFromString<BluetoothMessage>(decodedJsonString)
        debugLog { "Decoded message object: $message" }
        return message
    }
}