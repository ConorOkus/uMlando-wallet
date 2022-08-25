package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.Global
import com.example.umlandowallet.toHex

@Composable
fun NodeId() {
    val nodeId = remember { mutableStateOf("") }

    Button(onClick = {
        val nodeIdByteArray = Global.channelManager?._our_node_id

        nodeId.value = if (nodeIdByteArray != null) {
            println("NODE ID: ${nodeIdByteArray.toHex()}")
            nodeIdByteArray.toHex()
        } else {
            "Cannot retrieve node ID"
        }
    }) {
        Text(text = "Node ID")
    }
    Spacer(modifier = Modifier.height(8.dp))
    if(nodeId.value != "") {
        Text(text = nodeId.value, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
    }

}