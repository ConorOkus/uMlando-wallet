package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.Global
import com.example.umlandowallet.toByteArray
import org.ldk.structs.*

@Composable
fun FundChannel() {
    Button(onClick = {
        createChannel()
    }) {
        Text(text = "Fund Channel")
    }
    Spacer(modifier = Modifier.height(8.dp))
}

fun createChannel() {
    Global.temporaryChannelId = null;
    val peerNodePubkey = "0306599a163c56f41e6f28aafb77da7fcc573f958aa70bdab44d97ba373697f1a6"

    // public aka announced channel. such channels can route and thus have fees
    val userConfig = UserConfig.with_default()
    val newChannelConfig = ChannelConfig.with_default()
    newChannelConfig.set_forwarding_fee_proportional_millionths(10000);
    newChannelConfig.set_forwarding_fee_base_msat(1000);
    userConfig.set_channel_config(newChannelConfig);

    val createChannelResult = Global.channelManager!!.create_channel(
        peerNodePubkey.toByteArray(), 20000, 0, 1, userConfig);

    if (createChannelResult !is Result__u832APIErrorZ.Result__u832APIErrorZ_OK) {
        println("create_channel_result !is Result__u832APIErrorZ.Result__u832APIErrorZ_OK, = " + createChannelResult);
        println("Open Channel Request Failed");
    }

    println("Channel open request was sent: ${createChannelResult.is_ok}");
}
