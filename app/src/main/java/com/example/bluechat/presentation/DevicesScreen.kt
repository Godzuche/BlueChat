package com.example.bluechat.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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

@Composable
fun DevicesScreen(
    uiState: BluetoothUiState,
    onDeviceClick: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyGridState = rememberLazyGridState()

    LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Adaptive(150.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(text = "Paired Devices")
        }
        items(
            items = uiState.pairedDevices.toList(),
            span = { GridItemSpan(maxLineSpan) },
        ) { device ->
            Text(
                text = device.name ?: device.hardwareAddress,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeviceClick(device) },
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(text = "Available Devices")
        }
        items(
            items = uiState.scannedDevices.toList(),
            span = { GridItemSpan(maxLineSpan) },
        ) { device ->
            Text(
                text = device.name ?: device.hardwareAddress,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeviceClick(device) },
            )
        }
    }
}

@Preview
@Composable
fun DevicesScreenPreview() {
    BlueChatTheme {
        DevicesScreen(uiState = BluetoothUiState(), onDeviceClick = {})
    }
}