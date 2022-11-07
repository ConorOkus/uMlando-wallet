package com.example.umlandowallet.data.remote

import com.example.umlandowallet.data.*
import com.example.umlandowallet.toByteArray
import com.example.umlandowallet.toHex
import org.bitcoindevkit.Blockchain
import org.ldk.structs.ChainMonitor
import org.ldk.structs.ChannelManager
import org.ldk.structs.TwoTuple_usizeTransactionZ

class AccessImpl(
    private val blockchain: Blockchain,
) : Access {
    override suspend fun sync() {
        val currentHeight = blockchain.getHeight()
    }

    override suspend fun syncWallet(onchainWallet: OnchainWallet) {
        onchainWallet.sync()
    }

    override suspend fun syncBestBlockConnected(
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    ) {
        val service = Service.create()

        val height = service.getlatestBlockHeight()
        val hash = service.getlatestBlockHash()
        val header = service.getHeader(hash)

        channelManager.as_Confirm().best_block_updated(header.toByteArray(), height)
        chainMonitor.as_Confirm().best_block_updated(header.toByteArray(), height)
    }


    override suspend fun syncTransactionConfirmed(
        relevantTxIds: Array<ByteArray>,
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    ) {
        val service = Service.create()

        val confirmedTxs = mutableListOf<ConfirmedTx>()
        for (txid in relevantTxIds) {
            val txId = txid.reversedArray().toHex()
            val txStatus: TxStatus = service.getTxStatus(txId)
            if (txStatus.confirmed) {
                val txHex = service.getTxHex(txId)
                val tx = service.getTx(txId)
                if (tx.status.block_height != null) {
                    val blockHeader = service.getHeader(tx.status.block_hash)
                    val merkleProof = service.getMerkleProof(txId)
                    if (tx.status.block_height == merkleProof.block_height) {
                        confirmedTxs.add(ConfirmedTx(
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
        confirmedTxs.sortWith(compareBy<ConfirmedTx> { it.block_height }.thenBy { it.merkle_proof_pos })

        for (cTx in confirmedTxs) {
            channelManager.as_Confirm().transactions_confirmed(
                cTx.block_header.toByteArray(),
                arrayOf<TwoTuple_usizeTransactionZ>(TwoTuple_usizeTransactionZ.of(cTx.block_height.toLong(), cTx.tx)),
                cTx.block_height
            )

            chainMonitor.as_Confirm().transactions_confirmed(
                cTx.block_header.toByteArray(),
                arrayOf<TwoTuple_usizeTransactionZ>(TwoTuple_usizeTransactionZ.of(cTx.block_height.toLong(), cTx.tx)),
                cTx.block_height
            )
        }
    }

    override suspend fun syncTransactionsUnconfirmed(
        relevantTxIds: Array<ByteArray>,
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    ) {
        val service = Service.create()

        for (txid in relevantTxIds) {
            val txId = txid.reversedArray().toHex()
            val txStatus: TxStatus = service.getTxStatus(txId)
            if (!txStatus.confirmed) {
                channelManager.as_Confirm().transaction_unconfirmed(txId.toByteArray())
                chainMonitor.as_Confirm().transaction_unconfirmed(txId.toByteArray())
            }
        }
    }
}