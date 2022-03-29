package com.example.umlandowallet.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.example.umlandowallet.data.listPeers

@Composable
fun ListPeersScreen() {
    val peers = listPeers()

    Text(text = peers)
}