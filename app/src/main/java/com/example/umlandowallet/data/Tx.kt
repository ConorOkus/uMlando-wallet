package com.example.umlandowallet.data

import kotlinx.serialization.Serializable

@Serializable
data class Tx(
    val txid: String,
    val status: TxStatus
)

@Serializable
data class TxStatus(
    val confirmed: Boolean,
    val block_height: Int,
    val block_hash: String,
)
