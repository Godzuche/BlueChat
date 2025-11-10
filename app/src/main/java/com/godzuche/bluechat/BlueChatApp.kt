package com.godzuche.bluechat

import android.app.Activity.RESULT_CANCELED
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bluechat.R
import com.godzuche.bluechat.chat.presentation.BluetoothViewModel
import com.godzuche.bluechat.chat.presentation.ConnectionStatus
import com.godzuche.bluechat.chat.presentation.chat.chatRoute
import com.godzuche.bluechat.chat.presentation.chat.navigateToChat
import com.godzuche.bluechat.chat.presentation.device_list.devicesRoute
import com.godzuche.bluechat.core.design_system.components.ConnectingScreen
import com.godzuche.bluechat.core.presentation.util.Constants
import com.godzuche.bluechat.core.presentation.util.DiscoverabilityTimer
import com.godzuche.bluechat.core.presentation.util.debugLog
import com.godzuche.bluechat.core.presentation.util.showToast
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.rememberHazeState

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
    val hazeState = rememberHazeState()

    val backgroundColor = MaterialTheme.colorScheme.background
    val lightAlpha = 0.3f
    val darkAlpha = 0.1f
    val hazeStyle = HazeStyle(
        backgroundColor = backgroundColor,
        tints = listOf(
            HazeTint(
                backgroundColor.copy(
                    alpha = if (backgroundColor.luminance() >= 0.5) lightAlpha else darkAlpha
                ),
            )
        ),
        blurRadius = 10.dp,
        noiseFactor = 0f,
        fallbackTint = HazeTint.Unspecified,
    )

    val discoverabilityLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_CANCELED -> {
                    debugLog { "Bluetooth Discoverability declined by the user" }
                    context.showToast("Device discovery declined")
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
//                            bluetoothViewModel.stopListeningForIncomingConnections()
                            bluetoothViewModel.onCancelConnection()
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
            context.showToast(message)
        }
    }

    LaunchedEffect(key1 = uiState.connectionStatus) {
        when (uiState.connectionStatus) {
            ConnectionStatus.CONNECTED -> {
                context.showToast(R.string.connected_message)
                if (currentDestination?.route != chatRoute) {
                    navController.navigateToChat()
                }
            }

            ConnectionStatus.NOT_CONNECTED,
            ConnectionStatus.DISCONNECTED -> {
                context.showToast(R.string.disconnected)
                if (currentDestination?.route == chatRoute) {
                    context.showToast(R.string.disconnected)
                    navController.navigateUp()
                }
            }

            else -> Unit
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // For Haze background blur
                ),
                actions = {
                    when (currentDestination?.route) {
                        devicesRoute -> {
                            val buttonText = if (uiState.isDiscovering) {
                                stringResource(id = R.string.stop)
                            } else stringResource(id = R.string.scan)

                            AnimatedVisibility(
                                visible = /*!uiState.isConnecting && !uiState.isWaitingForConnection,*/
                                    uiState.connectionStatus !in listOf(
                                        ConnectionStatus.CONNECTING_TO_DEVICE,
                                        ConnectionStatus.WAITING_FOR_CONNECTION,
                                    ),
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
                                        onClick = if (uiState.isDiscovering) {
                                            bluetoothViewModel::stopScan
                                        } else {
                                            // Todo: Check if bluetooth is disabled and show a dialog to enable it
                                            bluetoothViewModel::startScan
                                        },
                                    ) {
                                        Text(text = buttonText)
                                    }
                                }
                            }
                        }

                        chatRoute -> {
                            IconButton(onClick = bluetoothViewModel::disconnectFromDevice) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                                    contentDescription = stringResource(id = R.string.disconnect),
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .hazeEffect(
                        state = hazeState,
                        style = hazeStyle,
                    )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentDestination?.route == devicesRoute && /*!uiState.isConnecting && !uiState.isWaitingForConnection,*/
                        uiState.connectionStatus !in listOf(
                    ConnectionStatus.CONNECTING_TO_DEVICE,
                    ConnectionStatus.WAITING_FOR_CONNECTION,
                ),
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
                            imageVector = ImageVector.vectorResource(R.drawable.ic_add_2),
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
//                .padding(scaffoldPadding) // Commented out for content to be drawn beneath status bar
                .consumeWindowInsets(scaffoldPadding)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal
                    )
                )
        ) {
            CompositionLocalProvider(
                LocalHazeState provides hazeState,
                LocalAppScaffoldPadding provides scaffoldPadding,
            ) {
                BlueChatNavHost(
                    bluetoothViewModel = bluetoothViewModel,
                    navController = navController,
                )
            }
        }

//        val infiniteTransition = rememberInfiniteTransition("connection-state-transition")
//        val ellipsisCount by infiniteTransition.animateFloat(
//            initialValue = 0f,
//            targetValue = 3f,
//            animationSpec = infiniteRepeatable(
//                animation = tween(durationMillis = 3000, delayMillis = 500, easing = LinearEasing),
//                repeatMode = RepeatMode.Reverse,
//            ),
//            label = "ellipsis",
//        )

        when (uiState.connectionStatus) {
            ConnectionStatus.CONNECTING_TO_DEVICE,
            ConnectionStatus.WAITING_FOR_CONNECTION,
            ConnectionStatus.INITIALIZING_CHAT -> {
                CompositionLocalProvider(
                    LocalHazeState provides hazeState,
                    LocalHazeStyle provides hazeStyle,
                ) {
                    ConnectingScreen(
                        loadingStatusMessage = when (uiState.connectionStatus) {
                            ConnectionStatus.CONNECTING_TO_DEVICE -> "Connecting to device"
                            ConnectionStatus.WAITING_FOR_CONNECTION -> "Waiting for device"
                            ConnectionStatus.INITIALIZING_CHAT -> "Initializing chat"
                            else -> null
                        },
                        canCancel = uiState.connectionStatus != ConnectionStatus.INITIALIZING_CHAT,
                        onCancelClick = {
//                            bluetoothViewModel.stopConnectingToDevice()
                            if (uiState.connectionStatus == ConnectionStatus.CONNECTING_TO_DEVICE) {
                                bluetoothViewModel.onCancelConnection()
                            }

                            if (uiState.connectionStatus == ConnectionStatus.WAITING_FOR_CONNECTION) {
                                DiscoverabilityTimer.stopDiscoverabilityCountdown(
                                    onStop = {
//                                    bluetoothViewModel.stopListeningForIncomingConnections()
                                        bluetoothViewModel.onCancelConnection()
                                    }
                                )
                            }
                        },
                    )
                }
            }

            else -> Unit
        }

    }

}

val LocalHazeState = compositionLocalOf { HazeState() }
val LocalAppScaffoldPadding = compositionLocalOf { PaddingValues(0.dp) }
