package com.godzuche.bluechat.core.presentation.util

import android.os.CountDownTimer

fun startDiscoverabilityCountdown(
    durationInSeconds: Int,
    intervalInSeconds: Int = 1,
    onTick: (Long) -> Unit = {},
    onFinish: () -> Unit = {},
) {
    val durationInMillis = durationInSeconds * 1000L
    val intervalInMillis = intervalInSeconds * 1000L
    val timer = object : CountDownTimer(durationInMillis, intervalInMillis) {
        override fun onTick(millisUntilFinished: Long) {
            val secondsLeft = millisUntilFinished / 1000L
            onTick(secondsLeft)
        }

        override fun onFinish() = onFinish()

    }
    timer.start()
}
