package com.godzuche.bluechat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.godzuche.bluechat.chat.presentation.BluetoothViewModel
import com.godzuche.bluechat.chat.presentation.chat.chatRoute
import com.godzuche.bluechat.chat.presentation.chat.chatScreen
import com.godzuche.bluechat.chat.presentation.device_list.devicesScreen

@Composable
fun BlueChatNavHost(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = chatRoute,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        devicesScreen(bluetoothViewModel)
        chatScreen(bluetoothViewModel)
    }
}