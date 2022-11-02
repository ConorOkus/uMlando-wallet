package com.example.umlandowallet.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umlandowallet.data.remote.Service
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectPeerScreen() {
    val service = Service.create()

    var pubKey by remember {
        mutableStateOf("")
    }
    val (connectPeerStatus, setConnectPeerStatus) = remember { mutableStateOf("") }

    Column(
        // verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    )
    {
        Text(
            text = "Connect to a Peer",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )
        TextField(
            value = pubKey,
            onValueChange = { pubKey = it },
            placeholder = {
                Text("Peer public node ID")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Button(
            onClick = {
                val pubKey = pubKey
                val host = "10.0.2.2"
                val port = 9500

                GlobalScope.launch {
                    val hasConnected = service.connectPeer(pubKey, host, port)
                    if(hasConnected) {
                        setConnectPeerStatus("Successfully connected to peer")
                    } else {
                        setConnectPeerStatus("Failed to connect")
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(text = "Connect")
        }
        if (connectPeerStatus.isNotBlank()) {
            Text(
                text = connectPeerStatus,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
