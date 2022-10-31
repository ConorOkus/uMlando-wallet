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

@Serializable
data class TxResponse(
    val txByteArray: ByteArray,
    val tx: Tx
)

data class ConfirmedTx(
    val tx: ByteArray,
    val block_height: Int,
    val block_header: String,
    val merkle_proof_pos: Int
)
