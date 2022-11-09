package com.example.umlandowallet.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umlandowallet.Global
import com.example.umlandowallet.toByteArray
import com.example.umlandowallet.toHex
import com.example.umlandowallet.ui.Screen
import org.ldk.structs.*
import java.time.temporal.TemporalAmount

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

                    var balance = 0L
                    balance = if (channel._outbound_capacity_msat == 0L) {
                        0L
                    } else {
                        channel._outbound_capacity_msat / channel._balance_msat
                    }
                    val nodeId = channel._counterparty._node_id.toHex()
                    val sendAmount = channel._outbound_capacity_msat / 1000
                    val receiveAmount = channel._inbound_capacity_msat / 1000

                    ListItem(
                        status,
                        nodeId,
                        balance.toFloat(),
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
    return Global.channelManager!!.list_usable_channels()
}

fun getLocalBalance(): String {
    val usableChannels = Global.channelManager!!.list_usable_channels()

    val localBalance = usableChannels.sumOf { it._balance_msat }

    return (localBalance / 1000).toString()
}