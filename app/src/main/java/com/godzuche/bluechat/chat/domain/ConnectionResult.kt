package com.godzuche.bluechat.chat.domain

sealed interface ConnectionResult {
    data object ConnectionEstablished : ConnectionResult
    data class Error(val errorMessage: String) : ConnectionResult
    data class TransferSucceeded(val message: String) : ConnectionResult
}