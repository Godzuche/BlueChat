package com.example.bluechat.chat.data.bluetooth

import android.bluetooth.BluetoothSocket
import com.example.bluechat.chat.domain.TransferFailedException
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
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext false
            }

            true
        }
    }
}