package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.data.remote.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectPeer() {
    val service = Service.create()

    var pubKey by remember {
        mutableStateOf("")
    }
    val (connectPeerStatus, setConnectPeerStatus) = remember { mutableStateOf("") }

    Button(
        onClick = {
            val pubKey = pubKey
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
    Column(verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 8.dp))
    {
        TextField(
            value = pubKey,
            onValueChange = { pubKey = it },
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (connectPeerStatus != "") {
        Text(connectPeerStatus)
        Spacer(modifier = Modifier.height(8.dp))
    }
}
