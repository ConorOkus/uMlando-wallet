package com.example.umlandowallet.ui

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import com.example.umlandowallet.Global
import com.example.umlandowallet.byteArrayToHex

@Composable
fun NodeId() {
    val nodeId = remember { mutableStateOf("") }

    Button(onClick = {
        val nodeIdByteArray = Global.channelManager?._our_node_id

        nodeId.value = if (nodeIdByteArray != null) {
            println("NODE ID: ${byteArrayToHex(nodeIdByteArray)}")
            byteArrayToHex(nodeIdByteArray)
        } else {
            "Cannot retrieve node ID"
        }
    }) {
        Text(text = "Node ID")
    }
    Text(text = nodeId.value, textAlign = TextAlign.Center)
}