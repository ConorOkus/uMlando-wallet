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
            val pubKey = "038863cf8ab91046230f561cd5b386cbff8309fa02e3f0c3ed161a3aeb64a643b9"
            val host = "203.132.94.196"
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
