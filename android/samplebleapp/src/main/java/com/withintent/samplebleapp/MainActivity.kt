package com.withintent.samplebleapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.polidea.multiplatformbleadapter.BleModule
import com.withintent.samplebleapp.ui.theme.AndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bleModule = BleModule(this)
        Log.i("BLE", bleModule.currentState)
        setContent {
            AndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column {
                        Text(
                            text = "Ble Sample APP", style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "BLE State ${bleModule.currentState}"
                        )
                    }
                }
            }
        }
    }
}