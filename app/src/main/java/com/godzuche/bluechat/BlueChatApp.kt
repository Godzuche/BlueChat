package com.godzuche.bluechat

import android.app.Activity.RESULT_CANCELED
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
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
import com.godzuche.bluechat.core.design_system.components.PhysicsRippleScanner
import com.godzuche.bluechat.core.presentation.util.Constants
import com.godzuche.bluechat.core.presentation.util.DiscoverabilityTimer
import com.godzuche.bluechat.core.presentation.util.debugLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlueChatApp(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
) {
    val uiState by bluetoothViewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val currentDestination: NavDestination? =
        navController.currentBackStackEntryAsState().value?.destination
    val context = LocalContext.current

    val discoverabilityLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_CANCELED -> {
                    debugLog { "Bluetooth Discoverability declined by the user" }
                    Toast.makeText(context, "Device discovery declined", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    debugLog { "Bluetooth Device is discoverable for ${result.resultCode} seconds" }
                    bluetoothViewModel.listenAndWaitForIncomingConnections()
                    DiscoverabilityTimer.startDiscoverabilityCountdown(
                        durationInSeconds = Constants.BLUETOOTH_DISCOVERABILITY_TIMEOUT_SECONDS,
                        onTick = { secondsLeft ->
                            debugLog { "Discoverability Discoverable for $secondsLeft seconds" }
                        },
                        onFinish = {
                            debugLog { "TimedOut: Discoverability Device is no longer discoverable." }
                            bluetoothViewModel.stopListeningForIncomingConnections()
                        }
                    )
                }
            }
        }

    fun makeDeviceDiscoverable(durationInSeconds: Int = Constants.BLUETOOTH_DISCOVERABILITY_TIMEOUT_SECONDS) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, durationInSeconds)
        }
        discoverabilityLauncher.launch(discoverableIntent)
    }

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

                            AnimatedVisibility(
                                visible = !uiState.isConnecting && !uiState.isWaiting,
                                enter = fadeIn() + slideInHorizontally(
                                    initialOffsetX = { it * 2 }
                                ),
                                exit = slideOutHorizontally(
                                    targetOffsetX = { it * 2 }
                                ) + fadeOut(),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (uiState.isDiscovering) {
                                        CircularProgressIndicator()
                                    }

                                    debugLog { "BT IsDiscovering " + uiState.isDiscovering.toString() }
                                    debugLog {
                                        "BT IsDiscoveryFinished " + uiState.isDiscoveringFinished.toString()
                                    }

                                    TextButton(
                                        onClick = if (uiState.isDiscovering)
                                            bluetoothViewModel::stopScan
                                        else bluetoothViewModel::startScan,
                                    ) {
                                        Text(text = buttonText)
                                    }
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
            AnimatedVisibility(
                visible = currentDestination?.route == devicesRoute && !uiState.isConnecting && !uiState.isWaiting,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                enter = fadeIn() + slideInHorizontally(
                    initialOffsetX = { it * 2 }
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { it * 2 }
                ) + fadeOut(),
            ) {
                FloatingActionButton(
                    onClick = { makeDeviceDiscoverable() },
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
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { scaffoldPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .consumeWindowInsets(scaffoldPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal
                    )
                )
        ) {
            BlueChatNavHost(
                bluetoothViewModel = bluetoothViewModel,
                navController = navController,
            )
        }

        when {
            uiState.isConnecting -> {
                PhysicsRippleScanner(modifier = Modifier.fillMaxSize())
            }

            uiState.isWaiting -> {
                Box {
                    PhysicsRippleScanner()
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-120).dp)
                            .clip(CircleShape)
                            .clickable {
                                DiscoverabilityTimer.stopDiscoverabilityCountdown(
                                    onStop = {
                                        bluetoothViewModel.stopListeningForIncomingConnections()
                                    }
                                )
                            }
                            .padding(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Stop waiting for incoming connections",
                            modifier = Modifier
                                .size(40.dp),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }
    }

}
