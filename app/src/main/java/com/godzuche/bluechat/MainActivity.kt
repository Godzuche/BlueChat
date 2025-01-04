package com.godzuche.bluechat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.bluechat.R
import com.godzuche.bluechat.core.data.util.haveAllPermissions
import com.godzuche.bluechat.chat.presentation.BluetoothViewModel
import com.godzuche.bluechat.core.design_system.theme.BlueChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<BluetoothViewModel>()

    private val bluetoothManager by lazy {
        applicationContext.getSystemService(BluetoothManager::class.java)
    }
    private val bluetoothAdapter by lazy {
        bluetoothManager?.adapter
    }

    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    @SuppressLint("InlinedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                Toast.makeText(
                    this,
                    R.string.bluetooth_turned_on_success_message,
                    Toast.LENGTH_LONG,
                ).show()
                viewModel.updatePairedDevices()
            } else {
                // Show a dialog explaining why the user has to turn on bluetooth.
            }
        }

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            /*val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true*/
            val grantedAll = perms.values.all { it }
            if (grantedAll) {
                if (/*canEnableBluetooth &&*/ isBluetoothEnabled.not()) {
                    enableBluetoothLauncher.launch(
                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    )
                }
            } else {
                // Todo: Handle rejection.
            }
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

        permissionLauncher.launch(
            /*arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
//                    Manifest.permission.BLUETOOTH,
            )*/
            ALL_BT_PERMISSIONS
        )
//        } else {
        /*enableBluetoothLauncher.launch(
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        )*/

        /*permissionLauncher.launch(
            arrayOf(
                *//*Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,*//*
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            )*/
//        }

        setContent {
            val isBluetoothEnabledAndPermissionGranted =
                isBluetoothEnabled && if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    LocalContext.current.haveAllPermissions(ALL_BT_PERMISSIONS)
                } else true

            Log.e("BTT", "IsBTEnabled: $isBluetoothEnabledAndPermissionGranted")

            LaunchedEffect(key1 = isBluetoothEnabledAndPermissionGranted) {
                viewModel.updatePairedDevices()
            }

            BlueChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BlueChatApp()
                }
            }
        }
    }

    private val discoverabilityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_CANCELED -> {
                    Log.d("Bluetooth", "Discoverability declined by the user.")
                }

                else -> {
                    Log.d("Bluetooth", "Device is discoverable for ${result.resultCode} seconds.")
                }
            }
        }

    private fun makeDeviceDiscoverable(durationInSeconds: Int = 120) { // 2 minutes
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, durationInSeconds)
        }
        discoverabilityLauncher.launch(discoverableIntent)
    }

    private fun startDiscoverabilityCountdown(durationInSeconds: Int) {
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

}

val ALL_BT_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.ACCESS_FINE_LOCATION,
    )
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    arrayOf(
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH,
    )
} else {
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH,
    )
}

val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
} else {
    arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}

