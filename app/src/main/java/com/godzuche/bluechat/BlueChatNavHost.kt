package com.godzuche.bluechat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.godzuche.bluechat.chat.presentation.BluetoothViewModel
import com.godzuche.bluechat.chat.presentation.chat.chatScreen
import com.godzuche.bluechat.chat.presentation.device_list.devicesRoute
import com.godzuche.bluechat.chat.presentation.device_list.devicesScreen

@Composable
fun BlueChatNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
    startDestination: String = devicesRoute,
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