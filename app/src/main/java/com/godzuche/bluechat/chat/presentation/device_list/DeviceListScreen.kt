package com.godzuche.bluechat.chat.presentation.device_list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
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
import androidx.compose.ui.draw.clip
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
    val listState = rememberLazyGridState()
    val noAvailableDevice =
        uiState.isDiscoveringFinished && uiState.scannedDevices.isEmpty()

    LaunchedEffect(uiState.isDiscovering) {
        if (!listState.isScrollInProgress && uiState.isDiscovering && (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0)) {
            listState.animateScrollToItem(0)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.systemBars))
        }

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
                key = { it.hardwareAddress + it.name },
            ) { device ->
                val deviceIndex = uiState.scannedDevices.indexOf(device)
                val shape = RoundedCornerShape(
                    topStart = if (deviceIndex == 0) 8.dp else 0.dp,
                    topEnd = if (deviceIndex == 0) 8.dp else 0.dp,
                    bottomStart = if (deviceIndex == uiState.scannedDevices.lastIndex) 8.dp else 0.dp,
                    bottomEnd = if (deviceIndex == uiState.scannedDevices.lastIndex) 8.dp else 0.dp,
                )

                Text(
                    text = device.name ?: device.hardwareAddress,
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                        .clip(shape)
                        .clickable { onDeviceClick(device) }
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
//                            shape = shape,
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
            key = { it.hardwareAddress + it.name + "dup" },
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

        item {
            Spacer(
                modifier = Modifier.windowInsetsBottomHeight(
                    WindowInsets.systemBars
                )
            )
        }

    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun DevicesScreenPreview() {
    BlueChatTheme {
        DevicesScreen(
            uiState = BluetoothUiState()
                .copy(
                    pairedDevices = previewPairedDevices,
                    scannedDevices = previewAvailableDevices,
                ),
            onDeviceClick = {},
        )
    }
}

private val previewPairedDevices = listOf(
    BluetoothDevice(
        uuid = null,
        name = "John Doe",
        "62:B4:FD:BO:CO:06",
    ),
    BluetoothDevice(
        uuid = null,
        name = "God'swill Jonathan",
        "72:B4:FD:BO:CO:06",
    ),
    BluetoothDevice(
        uuid = null,
        name = "John Doe",
        "62:B4:FD:BO:CO:07",
    ),
    BluetoothDevice(
        uuid = null,
        name = "God'swill Jonathan",
        "72:B4:FD:BO:CO:01",
    ),
    BluetoothDevice(
        uuid = null,
        name = "John Doe",
        "62:B4:FD:BO:CO:02",
    ),
    BluetoothDevice(
        uuid = null,
        name = "God'swill Jonathan",
        "72:B4:FD:BO:CO:11",
    ),
    BluetoothDevice(
        uuid = null,
        name = "God'swill Jonathan",
        "72:B4:FD:B8:CO:01",
    ),
    BluetoothDevice(
        uuid = null,
        name = "John Doe",
        "62:B4:FD:B1:CO:02",
    ),
    BluetoothDevice(
        uuid = null,
        name = "God'swill Jonathan",
        "72:B4:FZ:BO:CO:11",
    ),
)
private val previewAvailableDevices = listOf(
    BluetoothDevice(
        uuid = null,
        name = "Samsung s22",
        "B3:B4:F5:BO:CO:06",
    ),
    BluetoothDevice(
        uuid = null,
        name = null,
        "D0:5D:FD:BO:CO:06",
    ),
    BluetoothDevice(
        uuid = null,
        name = "Samsung s22",
        "B3:B4:F5:BO:C1:06",
    ),
    BluetoothDevice(
        uuid = null,
        name = null,
        "D0:5D:FD:BO:C2:06",
    ),
)
