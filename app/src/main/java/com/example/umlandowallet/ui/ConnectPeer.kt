package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.data.remote.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun ConnectPeer() {
    val service = Service.create()

    val (connectPeerStatus, setConnectPeerStatus) = remember { mutableStateOf("") }

    Button(
        onClick = {
            val pubKey = "0296e20fa99d2940b8b00117e65d27003f0d8f81a0c960f71a5466d1aadf5ea5ea"
            val host = "69.59.18.82"
            val port = 9735

            GlobalScope.launch {
                val hasConnected = service.connectPeer(pubKey, host, port)
                if(hasConnected) {
                    setConnectPeerStatus("Successfully connected to a peer")
                } else {
                    setConnectPeerStatus("Failed to connect to a peer")
                }
            }

        },
    ) {
        Text(text = "Connect Peer")
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (connectPeerStatus != "") {
        Text(connectPeerStatus)
        Spacer(modifier = Modifier.height(8.dp))
    }
}
