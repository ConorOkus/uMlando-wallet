package com.example.umlandowallet.data.remote

import com.example.umlandowallet.*
import com.example.umlandowallet.data.*
import com.example.umlandowallet.utils.toByteArray
import com.example.umlandowallet.utils.toHex
import org.ldk.structs.*

class AccessImpl: Access {
    override suspend fun sync() {
        this.syncWallet(OnchainWallet)

        val channelManager = Global.channelManager!!
        val chainMonitor = Global.chainMonitor!!

        val relevantTxIdsFromChannelManager = channelManager.as_Confirm()._relevant_txids
        val relevantTxIdsFromChainMonitor = chainMonitor.as_Confirm()._relevant_txids

        val relevantTxIds = relevantTxIdsFromChannelManager + relevantTxIdsFromChainMonitor

        val service = Service.create()

        val confirmedTxs = mutableListOf<ConfirmedTx>()

        // Sync unconfirmed transactions
        for (txid in relevantTxIds) {
            val txId = txid._a.reversedArray().toHex()
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
                } else {
                    channelManager.as_Confirm().transaction_unconfirmed(txId.toByteArray())
                    chainMonitor.as_Confirm().transaction_unconfirmed(txId.toByteArray())
                }
            }
        }

        // Add confirmed Tx from filtered Transaction Ids
        val filteredTxs = LDKTxFilter.txids
        if (filteredTxs.isNotEmpty() && filteredTxs !== null) {
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
    }

    override suspend fun syncWallet(onchainWallet: OnchainWallet) {
        onchainWallet.sync()
    }

}