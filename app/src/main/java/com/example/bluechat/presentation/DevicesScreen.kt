package com.example.bluechat.presentation

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bluechat.domain.BluetoothDevice
import com.example.bluechat.ui.theme.BlueChatTheme

@Composable
fun DevicesRoute(
    navigateToChat: () -> Unit,
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
) {
    val uiState by bluetoothViewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState.isConnected) {
        if (uiState.isConnected) {
            Toast.makeText(
                context,
                "You're connected",
                Toast.LENGTH_LONG
            ).show()
            navigateToChat()
        }
    }

    LaunchedEffect(key1 = uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    DevicesScreen(
        uiState = uiState,
        onDeviceClick = bluetoothViewModel::connectToDevice,
    )

    if (uiState.isConnecting) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DevicesScreen(
    uiState: BluetoothUiState,
    onDeviceClick: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    val noAvailableDevice =
        uiState.isDiscoveringFinished && uiState.scannedDevices.isEmpty()

    /*    val pairedDeviceListState = rememberLazyListState()
        val scannedDeviceListState = rememberLazyListState()*/

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        state = rememberLazyGridState(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Paired Devices",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
            )
        }

        items(
            items = uiState.pairedDevices.toList(),
            span = { GridItemSpan(maxLineSpan) }
        ) { device ->
            Text(
                text = device.name ?: device.hardwareAddress,
                modifier = Modifier
                    .animateItemPlacement()
                    .fillMaxWidth()
                    .clickable { onDeviceClick(device) }
                    .padding(20.dp),
            )
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = "Available Devices",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
            )
        }

        if (noAvailableDevice) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "No device found.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            items(
                items = uiState.scannedDevices.toList(),
                span = { GridItemSpan(maxLineSpan) },
            ) { device ->
                Text(
                    text = device.name ?: device.hardwareAddress,
                    modifier = Modifier
                        .animateItemPlacement()
                        .fillMaxWidth()
                        .clickable { onDeviceClick(device) }
                        .padding(20.dp),
                )
            }
        }

        /*    Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(modifier)
            ) {
                Text(
                    text = "Paired Devices",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        state = pairedDeviceListState,
                    ) {
                        items(
                            items = uiState.pairedDevices.toList(),
                        ) { device ->
                            Text(
                                text = device.name ?: device.hardwareAddress,
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .fillMaxWidth()
                                    .clickable { onDeviceClick(device) }
                                    .padding(20.dp),
                            )
                        }
                    }
                }

                Text(
                    text = "Available Devices",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                )

                if (noAvailableDevice) {
                    Text(
                        text = "No device found.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            state = scannedDeviceListState,
                        ) {
                            items(items = uiState.scannedDevices.toList()) { device ->
                                Text(
                                    text = device.name ?: device.hardwareAddress,
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .fillMaxWidth()
                                        .clickable { onDeviceClick(device) }
                                        .padding(20.dp),
                                )
                            }
                        }
                    }
                }
            }*/
    }
}

@Preview(showBackground = true)
@Composable
fun DevicesScreenPreview() {
    BlueChatTheme {
        DevicesScreen(uiState = BluetoothUiState(), onDeviceClick = {})
    }
}