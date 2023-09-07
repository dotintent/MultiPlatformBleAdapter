package com.withintent.samplebleapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.polidea.multiplatformbleadapter.BleAdapterFactory
import com.polidea.multiplatformbleadapter.ScanResult
import com.withintent.samplebleapp.ui.theme.AndroidTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bleAdapter = BleAdapterFactory.getNewAdapter(this.applicationContext)
        bleAdapter.createClient("SampleBleApp",
            {
                Log.i("BLE", bleAdapter.currentState)
                Log.i("BLE", "OnEventCallback $it")
            },
            {
                Log.i("BLE", "OnEventCallback $it")
            }
        )

        setContent {
            AndroidTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val devices = remember { mutableStateOf(mapOf<String, ScanResult>()) }
                    val scanStarted = remember { mutableStateOf(false) }

                    Column {
                        Text(
                            text = "Ble Sample APP", style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "BLE State ${bleAdapter.currentState}"
                        )

                        if (!scanStarted.value) {
                            Button(onClick = {
                                if (bleAdapter.currentState == "PoweredOn") {
                                    devices.value = emptyMap()
                                    scanStarted.value = true
                                    bleAdapter.startDeviceScan(
                                        null,
                                        0,
                                        1,
                                        {
                                            devices.value = devices.value.plus(it.deviceId to it)
                                        },
                                        {
                                            Log.i("BLE", "OnErrorCallback $it")
                                        }
                                    )
                                }
                            }) {
                                Text(text = "Start scan")
                            }
                        } else {
                            Button(onClick = {
                                scanStarted.value = false
                                bleAdapter.stopDeviceScan()
                            }) {
                                Text(text = "Stop scan")
                            }
                        }

                        Card(modifier = Modifier.padding(8.dp)) {
                            val sorted = devices.value.values.sortedBy { it.deviceId }
                            LazyColumn {
                                items(sorted.size) { index ->
                                    val item: ScanResult = sorted.get(index)
                                    Text(text = "${item.deviceId} - ${item.deviceName}", modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}