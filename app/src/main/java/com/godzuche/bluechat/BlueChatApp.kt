package com.godzuche.bluechat

import android.app.Activity.RESULT_CANCELED
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bluechat.R
import com.godzuche.bluechat.chat.presentation.BluetoothViewModel
import com.godzuche.bluechat.chat.presentation.chat.chatRoute
import com.godzuche.bluechat.chat.presentation.chat.navigateToChat
import com.godzuche.bluechat.chat.presentation.device_list.devicesRoute
import com.godzuche.bluechat.core.design_system.ui.component.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlueChatApp(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val currentDestination: NavDestination? =
        navController.currentBackStackEntryAsState().value?.destination
    val context = LocalContext.current

    val uiState by bluetoothViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(key1 = uiState.isConnected) {
        val messageRes = if (uiState.isConnected) {
            R.string.connected_message
        } else R.string.disconnected
        val toast = Toast.makeText(
            context,
            messageRes,
            Toast.LENGTH_LONG,
        )

        when {
            uiState.isConnected -> {
                toast.show()
                if (currentDestination?.route != chatRoute) {
                    navController.navigateToChat()
                }
            }

            !uiState.isConnected -> {
                if (currentDestination?.route == chatRoute) {
                    toast.show()
                    navController.navigateUp()
                }
            }
        }
    }

    val discoverabilityLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_CANCELED -> {
                    Log.d("Bluetooth", "Discoverability declined by the user.")
                }

                else -> {
                    Log.d("Bluetooth", "Device is discoverable for ${result.resultCode} seconds.")
                    startDiscoverabilityCountdown(durationInSeconds = 60)
                }
            }
        }

    fun makeDeviceDiscoverable(durationInSeconds: Int = 300) { // 2 minutes
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, durationInSeconds)
        }
        discoverabilityLauncher.launch(discoverableIntent)
    }

    val layoutDirection = LocalConfiguration.current.layoutDirection

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (currentDestination?.route == chatRoute) {
                        Text(
                            text = stringResource(id = R.string.messages),
                        )
                    }
                },
                actions = {
                    when (currentDestination?.route) {
                        devicesRoute -> {
                            val buttonText = if (uiState.isDiscovering) {
                                stringResource(id = R.string.stop)
                            } else stringResource(id = R.string.scan)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.isDiscovering) {
                                    CircularProgressIndicator()
                                }

                                Log.d("BT", "IsDiscovering" + uiState.isDiscovering.toString())
                                Log.d(
                                    "BT",
                                    "IsDiscoveryFinished" + uiState.isDiscoveringFinished.toString()
                                )

                                TextButton(
                                    onClick = if (uiState.isDiscovering)
                                        bluetoothViewModel::stopScan
                                    else bluetoothViewModel::startScan,
                                ) {
                                    Text(text = buttonText)
                                }
                            }
                        }

                        chatRoute -> {
                            IconButton(onClick = bluetoothViewModel::disconnectFromDevice) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.disconnect),
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentDestination?.route == devicesRoute) {
                FloatingActionButton(
                    onClick = {
                        bluetoothViewModel.listenAndWaitForIncomingConnections()
                        makeDeviceDiscoverable()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Text(
                            text = stringResource(id = R.string.host_chat),
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = stringResource(id = R.string.host_chat)
                        )
                    }
                }
            }
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
//                .consumeWindowInsets(scaffoldPadding)
        ) {
            BlueChatNavHost(
                bluetoothViewModel = bluetoothViewModel,
                navController = navController,
            )
        }

        if (uiState.isConnecting) {
            LoadingScreen(modifier = Modifier.fillMaxSize())
        }

    }

}

fun startDiscoverabilityCountdown(durationInSeconds: Int) {
    val timer = object : CountDownTimer(durationInSeconds * 1000L, 1000L) {
        override fun onTick(millisUntilFinished: Long) {
            val secondsLeft = millisUntilFinished / 1000
            Log.d("Discoverability", "Discoverable for $secondsLeft seconds")
        }

        override fun onFinish() {
            Log.d("Discoverability", "Device is no longer discoverable.")
        }
    }
    timer.start()
}