package com.example.umlandowallet.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import org.bitcoindevkit.*

@Composable
fun MainScreen() {


    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 50.dp)
    ) {
        Wallet()
        ConnectPeer()
        ListPeers()
        NodeId()

    }
}


