package com.example.umlandowallet.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umlandowallet.Global
import com.example.umlandowallet.utils.toHex
import org.ldk.structs.TwoTuple_PublicKeyCOption_SocketAddressZZ

@Composable
fun ListPeersScreen() {
    Column(
        modifier = Modifier
            .padding(top = 48.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Currently Connected Peers",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )

        val peerManager = Global.channelManagerConstructor!!.peer_manager
        val peers = peerManager._peer_node_ids
        if (peers.isEmpty()) {
            Text(
                text = "No peers connected",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp)
            )
        }

        val peersList: MutableList<TwoTuple_PublicKeyCOption_SocketAddressZZ> = mutableListOf()
        peers.forEach {
            peersList.add(it)
        }
        peersList.forEach { peer ->
            Text(
                text = peer._a.toHex(),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp)
            )
        }
    }
}

