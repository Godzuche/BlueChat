package com.godzuche.bluechat

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.godzuche.bluechat.chat.presentation.devices.devicesRoute
import com.godzuche.bluechat.design_system.ui.component.LoadingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlueChatApp(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val currentDestination: NavDestination? =
        navController.currentBackStackEntryAsState().value?.destination

    val uiState by bluetoothViewModel.state.collectAsStateWithLifecycle()

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
                FloatingActionButton(onClick = bluetoothViewModel::listenAndWaitForIncomingConnections) {
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
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .consumeWindowInsets(it),
        ) {
            BlueChatNavHost(navController = navController)
        }

        if (uiState.isConnecting) {
            LoadingScreen(modifier = Modifier.fillMaxSize())
        }

    }
}