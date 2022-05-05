package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.byteArrayToHex
import com.example.umlandowallet.data.listPeers

@Composable
fun ListPeersScreen() {
    val peers = listPeers()

    LazyColumn(Modifier.fillMaxWidth().padding(8.dp)) {
        items(peers) { peer ->
            Text(text = byteArrayToHex(peer))
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
        }
    }
}