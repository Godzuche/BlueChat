package com.godzuche.bluechat.chat.presentation.device_list

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.godzuche.bluechat.chat.presentation.BluetoothViewModel

const val devicesRoute = "devices"

fun NavController.navigateToDeviceList(navOptions: NavOptions? = null) {
    navigate(devicesRoute, navOptions)
}

fun NavGraphBuilder.devicesScreen(bluetoothViewModel: BluetoothViewModel) {
    composable(devicesRoute) {
        DevicesRoute(bluetoothViewModel = bluetoothViewModel)
    }
}