package com.example.umlandowallet.data.remote

import android.util.Log
import com.example.umlandowallet.*
import com.example.umlandowallet.data.*
import com.example.umlandowallet.utils.LDKTAG
import com.example.umlandowallet.utils.toByteArray
import com.example.umlandowallet.utils.toHex
import org.ldk.structs.*

class AccessImpl: Access {
    override suspend fun sync() {
        this.syncWallet(OnchainWallet)
        Log.i(LDKTAG, "Attempting to sync on chain wallet")

        val channelManager = Global.channelManager!!
        val chainMonitor = Global.chainMonitor!!

        Log.i(LDKTAG, "Attempting to sync lightning wallet")

        val service = Service.create()

        val confirmedTxs = mutableListOf<ConfirmedTx>()

        // Sync unconfirmed transactions
        val relevantTxs = Global.relevantTxs
        for (transaction in relevantTxs) {
            val txId = transaction.id.reversedArray().toHex()
        Log.i(LDKTAG, "Finding relevant TXs the size is ${relevantTxIds.size}")
        // Sync unconfirmed transactions
        for (txid in relevantTxIds) {
            Log.i(LDKTAG, "Checking relevant TXs")
            val txId = txid._a.reversedArray().toHex()
            val tx: Tx = service.getTx(txId)
            if (tx.status.confirmed) {
                Log.i(LDKTAG, "Adding Confirmed TX")
                val txHex = service.getTxHex(txId)
                val blockHeader = service.getHeader(tx.status.block_hash)
                val merkleProof = service.getMerkleProof(txId)
                if (tx.status.block_height === merkleProof.block_height) {
                    Log.i(LDKTAG, "Adding Confirmed TX")
                    confirmedTxs.add(
                        ConfirmedTx(
                            tx = txHex.toByteArray(),
                            block_height = tx.status.block_height,
                            block_header = blockHeader,
                            merkle_proof_pos = merkleProof.pos
                        )
                    )
                }
            } else {
                Log.i(LDKTAG, "Marking unconfirmed TX")
                channelManager.as_Confirm().transaction_unconfirmed(txId.toByteArray())
                chainMonitor.as_Confirm().transaction_unconfirmed(txId.toByteArray())
            }
        }

        // Add confirmed Tx from filter Transaction Output
        val relevantOutputs = Global.relevantOutputs
        if (relevantOutputs.isNotEmpty()) {
            for (output in relevantOutputs) {
                val outpoint = output._outpoint
                val outputIndex = outpoint._index
                val txId = outpoint._txid.reversedArray().toHex()
                val outputSpent: OutputSpent = service.getOutputSpent(txId, outputIndex.toInt())
                if (outputSpent.spent) {
                    val tx: Tx = service.getTx(txId)
                    if (tx.status.confirmed) {
                        val txHex = service.getTxHex(txId)
                        val blockHeader = service.getHeader(tx.status.block_hash)
                        val merkleProof = service.getMerkleProof(txId)
                        if (tx.status.block_height == merkleProof.block_height) {
                            confirmedTxs.add(
                                ConfirmedTx(
                                    tx = txHex.toByteArray(),
                                    block_height = tx.status.block_height,
                                    block_header = blockHeader,
                                    merkle_proof_pos = merkleProof.pos
                                )
                            )
                        }
                    }
                }

            }

        }

        // Add confirmed Tx from filtered Transaction Ids
        val filteredTxs = LDKTxFilter.txids
        if (filteredTxs.isNotEmpty() && filteredTxs !== null) {
            Log.i(LDKTAG, "Getting Filtered TXs")
            for (txid in filteredTxs) {
                val txId = txid.reversedArray().toHex()
                val tx: Tx = service.getTx(txId)
                if (tx.status.confirmed) {
                    val txHex = service.getTxHex(txId)
                    val blockHeader = service.getHeader(tx.status.block_hash)
                    val merkleProof = service.getMerkleProof(txId)
                    if (tx.status.block_height === merkleProof.block_height) {
                        confirmedTxs.add(
                            ConfirmedTx(
                                tx = txHex.toByteArray(),
                                block_height = tx.status.block_height,
                                block_header = blockHeader,
                                merkle_proof_pos = merkleProof.pos
                            )
                        )
                    }
                }
            }
        }

        // Add confirmed Tx from filter Transaction Output
        val filteredOutputs = LDKTxFilter.outputs
        if (filteredOutputs.isNotEmpty() && filteredOutputs !== null) {
            for (output in filteredOutputs) {
                val outpoint = output._outpoint
                val outputIndex = outpoint._index
                val txId = outpoint._txid.reversedArray().toHex()
                val outputSpent: OutputSpent = service.getOutputSpent(txId, outputIndex.toInt())
                if (outputSpent.spent) {
                    val tx: Tx = service.getTx(outputSpent.txid)
                    if (tx.status.confirmed) {
                        val txHex = service.getTxHex(txId)
                        val blockHeader = service.getHeader(tx.status.block_hash)
                        val merkleProof = service.getMerkleProof(txId)
                        if (tx.status.block_height === merkleProof.block_height) {
                            confirmedTxs.add(
                                ConfirmedTx(
                                    tx = txHex.toByteArray(),
                                    block_height = tx.status.block_height,
                                    block_header = blockHeader,
                                    merkle_proof_pos = merkleProof.pos
                                )
                            )
                        }
                    }
                }

            }

        }

        confirmedTxs.sortWith(compareBy<ConfirmedTx> { it.block_height }.thenBy { it.merkle_proof_pos })

        // Sync confirmed transactions
        for (cTx in confirmedTxs) {
            channelManager.as_Confirm().transactions_confirmed(
                cTx.block_header.toByteArray(),
                arrayOf<TwoTuple_usizeTransactionZ>(
                    TwoTuple_usizeTransactionZ.of(
                        cTx.merkle_proof_pos.toLong(),
                        cTx.tx
                    )
                ),
                cTx.block_height
            )

            chainMonitor.as_Confirm().transactions_confirmed(
                cTx.block_header.toByteArray(),
                arrayOf<TwoTuple_usizeTransactionZ>(
                    TwoTuple_usizeTransactionZ.of(
                        cTx.merkle_proof_pos.toLong(),
                        cTx.tx
                    )
                ),
                cTx.block_height
            )
        }


        // Sync best block
        val blockHeight = service.getLatestBlockHeight()
        val blockHash = service.getLatestBlockHash()
        val blockHeader = service.getHeader(blockHash)

        channelManager.as_Confirm().best_block_updated(blockHeader.toByteArray(), blockHeight)
        chainMonitor.as_Confirm().best_block_updated(blockHeader.toByteArray(), blockHeight)

        Global.channelManagerConstructor!!.chain_sync_completed(LDKEventHandler, true)

        Log.i(LDKTAG, "Wallet synced")
    }

    override suspend fun syncWallet(onchainWallet: OnchainWallet) {
        onchainWallet.sync()
    }

}