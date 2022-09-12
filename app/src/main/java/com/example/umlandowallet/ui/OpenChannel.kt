package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.Global
import com.example.umlandowallet.toByteArray
import org.ldk.structs.*

@Composable
fun OpenChannel() {
    var rawTransaction by remember {
        mutableStateOf("")
    }

    Button(onClick = {
        Global.channelManager!!.funding_transaction_generated(
            Global.temporaryChannelId,
            Global.counterpartyNodeId,
            rawTransaction.toByteArray()
        )
    }) {
        Text(text = "Open Channel")
    }
    Column(verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 8.dp))
    {
        TextField(
            value = rawTransaction,
            onValueChange = { rawTransaction = it },
            modifier = Modifier.fillMaxWidth()
        )
    }
}