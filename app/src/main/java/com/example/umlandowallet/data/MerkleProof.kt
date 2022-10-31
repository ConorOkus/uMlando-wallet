package com.example.umlandowallet.data

import kotlinx.serialization.Serializable

@Serializable
data class MerkleProof(
    val block_height: Int,
    val merkle: Array<String>,
    val pos: Int
)
