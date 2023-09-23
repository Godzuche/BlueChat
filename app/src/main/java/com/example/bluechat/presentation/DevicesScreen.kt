package com.example.bluechat.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bluechat.domain.BluetoothDevice
import com.example.bluechat.ui.theme.BlueChatTheme

@Composable
fun DevicesRoute(
    bluetoothViewModel: BluetoothViewModel = hiltViewModel(),
) {
    val uiState by bluetoothViewModel.state.collectAsState()

    DevicesScreen(
        uiState = uiState,
        onDeviceClick = bluetoothViewModel::connectToDevice,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DevicesScreen(
    uiState: BluetoothUiState,
    onDeviceClick: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pairedDeviceListState = rememberLazyListState()
    val scannedDeviceListState = rememberLazyListState()

    Column(
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
}

@Preview(showBackground = true)
@Composable
fun DevicesScreenPreview() {
    BlueChatTheme {
        DevicesScreen(uiState = BluetoothUiState(), onDeviceClick = {})
    }
}