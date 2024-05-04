package com.example.bluechat.chat.presentation.devices

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val devicesRoute = "devices"

fun NavGraphBuilder.devicesScreen(
    navigateToChat: () -> Unit,
) {
    composable(devicesRoute) {
        DevicesRoute(navigateToChat = navigateToChat)
    }
}