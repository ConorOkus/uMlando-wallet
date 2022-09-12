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
            val pubKey = "0306599a163c56f41e6f28aafb77da7fcc573f958aa70bdab44d97ba373697f1a6"
            val host = "10.0.2.2"
            val port = 9500

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
