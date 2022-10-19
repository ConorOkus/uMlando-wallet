package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.Global
import com.example.umlandowallet.toByteArray
import org.ldk.structs.ChannelHandshakeConfig
import org.ldk.structs.Result__u832APIErrorZ
import org.ldk.structs.UserConfig

@Composable
fun FundChannel() {
    var pubKey by remember {
        mutableStateOf("")
    }

    Button(onClick = {
        createChannel(pubKey)
    }) {
        Text(text = "Fund Channel")
    }
    Spacer(modifier = Modifier.height(8.dp))
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
}

fun createChannel(pubKey: String) {
    Global.temporaryChannelId = null;

    val amount = 100_000L
    val pushMsat = 1_000L
    val userId = 42L

    // public aka announced channel
    val userConfig = UserConfig.with_default()

    val channelHandshakeConfig = ChannelHandshakeConfig.with_default()
    channelHandshakeConfig._announced_channel = true

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
