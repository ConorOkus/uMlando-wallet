package com.example.umlandowallet.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umlandowallet.Global
import com.example.umlandowallet.utils.LDKTAG
import com.example.umlandowallet.utils.toHex
import org.ldk.structs.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListChannelsScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 48.dp,
                start = 32.dp,
                end = 32.dp
            )
    )
    {
        Text(
            text = "Local balance sats: ${getLocalBalance()} ",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
        )
        Spacer(modifier = Modifier.size(32.dp))
        Text(
            text = "Channels",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
        )
        Spacer(modifier = Modifier.size(24.dp))
        val usableChannels = getUsuableChannels()

        if (usableChannels != null) {
            if (usableChannels.isNotEmpty()) {
                usableChannels.forEach { channel ->
                    val status = channel._is_channel_ready

                    val nodeId = channel._counterparty._node_id.toHex()
                    val sendAmount = channel._outbound_capacity_msat / 1000
                    val receiveAmount = channel._inbound_capacity_msat / 1000
                    val progress: Double = receiveAmount.toDouble() / (sendAmount.toDouble() + receiveAmount.toDouble())
                    Log.i(LDKTAG, "Progress update: $progress with $sendAmount and $receiveAmount")

                    ListItem(
                        status,
                        nodeId,
                        progress.toFloat(),
                        sendAmount.toString(),
                        receiveAmount.toString()
                    )
                }
            }
        }


    }
}

@Composable
fun ListItem(
    status: Boolean,
    nodeId: String,
    progress: Float,
    sendAmount: String,
    receiveAmount: String
) {
    var progress by remember { mutableStateOf(progress) }

    if (status) {
        Text(text = "Active", style = TextStyle(color = Color.Green))
    }
    Text(text = nodeId)
    LinearProgressIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp)
            .padding(top = 16.dp),
        progress = progress
    )
    Row(
        modifier = Modifier
            .fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "$sendAmount sats")
        Text(text = "$receiveAmount sats")
    }
    Spacer(modifier = Modifier.size(16.dp))


}

fun getUsuableChannels(): Array<out ChannelDetails>? {
    return Global.channelManager!!.list_channels()
}

fun getLocalBalance(): String {
    val usableChannels = Global.channelManager!!.list_channels()

    val localBalance = usableChannels.sumOf { it._balance_msat }

    return (localBalance / 1000).toString()
}