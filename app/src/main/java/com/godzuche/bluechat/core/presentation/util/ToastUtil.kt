package com.godzuche.bluechat.core.presentation.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(
    message: String,
) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT)
        .show()
}

fun Context.showToast(
    @StringRes
    messageResId: Int,
) {
    Toast.makeText(this, messageResId, Toast.LENGTH_SHORT)
        .show()
}