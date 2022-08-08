package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.Global
import com.example.umlandowallet.byteArrayToHex
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ListPeers() {
    val _peerList = remember { MutableStateFlow(listOf<String>()) }
    val peerList by remember { _peerList }.collectAsState()
    Button(
        onClick = {
            val peers = Global.peerManager!!.get_peer_node_ids()
            val newList = ArrayList(peerList)

            peers.iterator().forEach {
                newList.add(byteArrayToHex(it))
            }

            _peerList.value = newList
        },
    ) {
        Text(text = "List Peers")
    }
    LazyColumn(Modifier.fillMaxWidth().padding(8.dp)) {
        items(peerList) { peer ->
                Text(text = peer)
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
            }
    }
}

