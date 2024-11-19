package com.godzuche.bluechat.chat.presentation.chat

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val chatRoute = "chat"

fun NavController.navigateToChat(navOptions: NavOptions? = null) {
    navigate(chatRoute, navOptions)
}

fun NavGraphBuilder.chatScreen() {
    composable(chatRoute) {
        ChatRoute()
    }
}