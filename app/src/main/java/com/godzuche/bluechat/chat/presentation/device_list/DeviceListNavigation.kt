package com.godzuche.bluechat.chat.presentation.device_list

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