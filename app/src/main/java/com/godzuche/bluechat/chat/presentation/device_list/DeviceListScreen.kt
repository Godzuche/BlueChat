package com.godzuche.bluechat.chat.presentation.device_list

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bluechat.R
import com.godzuche.bluechat.chat.domain.BluetoothDevice
import com.godzuche.bluechat.chat.presentation.BluetoothUiState
import com.godzuche.bluechat.chat.presentation.BluetoothViewModel
import com.godzuche.bluechat.core.design_system.theme.BlueChatTheme
import com.godzuche.bluechat.core.design_system.ui.component.LoadingScreen

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
                R.string.connected_message,
                Toast.LENGTH_LONG,
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
        LoadingScreen(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun DevicesScreen(
    uiState: BluetoothUiState,
    onDeviceClick: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier,
) {
    val noAvailableDevice =
        uiState.isDiscoveringFinished && uiState.scannedDevices.isEmpty()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        state = rememberLazyGridState(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(id = R.string.available_devices),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
            )
        }

        if (noAvailableDevice) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(id = R.string.no_device_found),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            items(
                items = uiState.scannedDevices,
                span = { GridItemSpan(maxLineSpan) },
            ) { device ->
                val deviceIndex = uiState.scannedDevices.indexOf(device)

                Text(
                    text = device.name ?: device.hardwareAddress,
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .clickable { onDeviceClick(device) }
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(
                                topStart = if (deviceIndex == 0) 8.dp else 0.dp,
                                topEnd = if (deviceIndex == 0) 8.dp else 0.dp,
                                bottomStart = if (deviceIndex == uiState.scannedDevices.lastIndex) 8.dp else 0.dp,
                                bottomEnd = if (deviceIndex == uiState.scannedDevices.lastIndex) 8.dp else 0.dp,
                            )
                        )
                        .padding(20.dp),
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                text = stringResource(id = R.string.paired_devices),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
            )
        }

        items(
            items = uiState.pairedDevices.toList(),
            span = { GridItemSpan(maxLineSpan) },
        ) { device ->
            Text(
                text = device.name ?: device.hardwareAddress,
                modifier = Modifier
                    .animateItem()
                    .fillMaxWidth()
                    .clickable { onDeviceClick(device) }
                    .padding(20.dp),
            )
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