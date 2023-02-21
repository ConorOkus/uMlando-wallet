package com.example.umlandowallet.data.remote

import com.example.umlandowallet.*
import com.example.umlandowallet.data.*
import com.example.umlandowallet.utils.toByteArray
import com.example.umlandowallet.utils.toHex
import org.ldk.structs.ChainMonitor
import org.ldk.structs.ChannelManager
import org.ldk.structs.TwoTuple_TxidBlockHashZ
import org.ldk.structs.TwoTuple_usizeTransactionZ

class AccessImpl: Access {
    override suspend fun sync() {
        this.syncWallet(OnchainWallet)

        val relevantTxIdsFromChannelManager = Global.channelManager!!.as_Confirm()._relevant_txids
        val relevantTxIdsFromChainMonitor = Global.chainMonitor!!.as_Confirm()._relevant_txids

        val relevantTxIds = relevantTxIdsFromChannelManager + relevantTxIdsFromChainMonitor

        this.syncTransactionsUnconfirmed(
            relevantTxIds,
            Global.channelManager!!,
            Global.chainMonitor!!
        )

        this.syncTransactionConfirmed(relevantTxIds, Global.channelManager!!, Global.chainMonitor!!)
        this.syncBestBlockConnected(Global.channelManager!!, Global.chainMonitor!!)

        Global.channelManagerConstructor!!.chain_sync_completed(
            ChannelManagerEventHandler, Global.scorer!!
        )
    }

    override suspend fun syncWallet(onchainWallet: OnchainWallet) {
        onchainWallet.sync()
    }

    override suspend fun syncBestBlockConnected(
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    ) {
        val service = Service.create()

        val height = service.getLatestBlockHeight()
        val hash = service.getLatestBlockHash()
        val header = service.getHeader(hash)

        channelManager.as_Confirm().best_block_updated(header.toByteArray(), height)
        chainMonitor.as_Confirm().best_block_updated(header.toByteArray(), height)
    }


    override suspend fun syncTransactionConfirmed(
        relevantTxIds: Array<TwoTuple_TxidBlockHashZ>,
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    ) {
        val service = Service.create()

        val confirmedTxs = mutableListOf<ConfirmedTx>()
        for (txid in relevantTxIds) {
            val txId = txid._a.reversedArray().toHex()
            val tx = service.getTx(txId)
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
        confirmedTxs.sortWith(compareBy<ConfirmedTx> { it.block_height }.thenBy { it.merkle_proof_pos })

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
    }

    override suspend fun syncTransactionsUnconfirmed(
        relevantTxIds: Array<TwoTuple_TxidBlockHashZ>,
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    ) {
        val service = Service.create()

        for (txid in relevantTxIds) {
            val txId = txid._a.reversedArray().toHex()
            val tx: Tx = service.getTx(txId)
            if (tx.status.confirmed) {
                channelManager.as_Confirm().transaction_unconfirmed(txId.toByteArray())
                chainMonitor.as_Confirm().transaction_unconfirmed(txId.toByteArray())
            }
        }
    }
}