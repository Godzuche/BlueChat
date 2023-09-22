package com.example.bluechat

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bluechat.presentation.BluetoothViewModel
import com.example.bluechat.presentation.DevicesRoute

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BlueChatApp(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
) {
    val uiState by bluetoothViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isDiscovering) {
                            CircularProgressIndicator()
                        }

                        Log.d("BT", "IsDiscovering" + uiState.isDiscovering.toString())
                        Log.d("BT", "IsDiscoveryFinished" + uiState.isDiscoveringFinished.toString())

                        val buttonText = if (uiState.isDiscovering) "Stop" else "Start"
                        TextButton(
                            onClick = if (uiState.isDiscovering) bluetoothViewModel::stopScan else bluetoothViewModel::startScan,
                        ) {
                            Text(text = buttonText)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = bluetoothViewModel::listenAndWaitForIncommingConnections) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = "Host Chat")
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .consumeWindowInsets(it)
        ) {
            DevicesRoute()
        }
    }
}