package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umlandowallet.Global
import com.example.umlandowallet.toByteArray
import org.ldk.structs.ChannelHandshakeConfig
import org.ldk.structs.Result__u832APIErrorZ
import org.ldk.structs.UserConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenChannelScreen() {
    var pubKey by remember {
        mutableStateOf("")
    }

    Column(
        // verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    )
    {
        Text(
            text = "Open a channel",
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
                createChannel(pubKey)
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(text = "Connect")
        }
    }
}


fun createChannel(pubKey: String) {
    Global.temporaryChannelId = null;

    val amount = 100_000L
    val pushMsat = 1_000L
    val userId = 42L

    // public aka announced channel
    val userConfig = UserConfig.with_default()

    val channelHandshakeConfig = ChannelHandshakeConfig.with_default()
    channelHandshakeConfig._announced_channel = false

    userConfig._channel_handshake_config = channelHandshakeConfig

    val createChannelResult = Global.channelManager!!.create_channel(
        pubKey.toByteArray(), amount, pushMsat, userId, userConfig
    )

    if (createChannelResult !is Result__u832APIErrorZ.Result__u832APIErrorZ_OK) {
        println("ERROR: failed to open channel with: $pubKey");
    }

    if(createChannelResult.is_ok) {
        println("EVENT: initiated channel with peer: $pubKey");
    }
}
