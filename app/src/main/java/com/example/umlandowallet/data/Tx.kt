package com.example.umlandowallet.data

import kotlinx.serialization.Serializable

class WatchedTransaction(val id: ByteArray, val scriptPubKey: ByteArray)

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

data class ConfirmedTx(
    val tx: ByteArray,
    val block_height: Int,
    val block_header: String,
    val merkle_proof_pos: Int
)
@Serializable
data class OutputSpent(
    val spent: Boolean,
)