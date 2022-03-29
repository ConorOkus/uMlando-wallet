package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.umlandowallet.data.remote.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Composable
fun ConnectPeerScreen(navController: NavController) {
    val service = Service.create()

    var pubKey by remember {
        mutableStateOf("")
    }
    var host by remember {
        mutableStateOf("")
    }
    var port by remember {
        mutableStateOf("")
    }
    val (connectPeerStatus, setConnectPeerStatus) = remember { mutableStateOf("Enter Credentials") }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 50.dp)
    ) {
        Text(connectPeerStatus, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            label = { Text(text = "Pub Key") },
            value = pubKey,
            onValueChange = { pubKey = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            label = { Text(text = "Host") },
            value = host,
            onValueChange = { host = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            label = { Text(text = "Port") },
            value = port,
            onValueChange = { port = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                GlobalScope.launch {
                    val hasConnected = service.connectPeer(pubKey, host, port.toInt())
                    if(hasConnected) {
                        setConnectPeerStatus("Successfully connected to peer")
                    } else {
                        setConnectPeerStatus("Failed to connect to peer")
                    }
                }

            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Connect Peer")

        }

    }
}
