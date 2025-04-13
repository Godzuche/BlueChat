package com.godzuche.bluechat.chat.data.bluetooth

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.godzuche.bluechat.chat.domain.TransferFailedException
import com.godzuche.bluechat.core.presentation.util.debugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothDataTransferService(
    private val socket: BluetoothSocket,
) {
    fun listenForIncomingData(): Flow<String> {
        return flow {
            if (socket.isConnected.not()) {
                debugLog { "Chat socket not connected" }
                return@flow
            }
            val buffer = ByteArray(1024)
            while (true) {
                val numBytes = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    e.printStackTrace()
                    throw TransferFailedException()
                }

                emit(
                    buffer.decodeToString(endIndex = numBytes)
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
//                socket.outputStream.flush()
            } catch (e: IOException) {
                e.printStackTrace()
                debugLog { "Chat Error occurred when sending data  $e" }
                return@withContext false
            }

            true
        }
    }
}