package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.Global
import com.example.umlandowallet.toByteArray

@OptIn(ExperimentalMaterial3Api::class)
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
