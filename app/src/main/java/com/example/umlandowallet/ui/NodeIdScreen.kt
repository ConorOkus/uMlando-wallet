package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.example.umlandowallet.Global
import com.example.umlandowallet.byteArrayToHex

@Composable
fun NodeIdScreen() {
    val nodeId = remember { mutableStateOf("") }

    LaunchedEffect(key1 = nodeId) {
        val nodeIdByteArray = Global.channelManager?._our_node_id

        nodeId.value = if (nodeIdByteArray != null) {
            println("NODE ID: ${byteArrayToHex(nodeIdByteArray)}")
            byteArrayToHex(nodeIdByteArray)
        } else {
            "Cannot retrieve node ID"
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        Text(text = nodeId.value, textAlign = TextAlign.Center)
    }
}