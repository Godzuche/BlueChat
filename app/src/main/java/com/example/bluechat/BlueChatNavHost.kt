package com.example.bluechat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.bluechat.chat.presentation.chat.chatScreen
import com.example.bluechat.chat.presentation.chat.navigateToChat
import com.example.bluechat.chat.presentation.devices.devicesRoute
import com.example.bluechat.chat.presentation.devices.devicesScreen

@Composable
fun BlueChatNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = devicesRoute,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        devicesScreen { navController.navigateToChat() }

        chatScreen()
    }
}