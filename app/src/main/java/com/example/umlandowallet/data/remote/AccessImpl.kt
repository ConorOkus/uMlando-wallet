package com.example.umlandowallet.data.remote

import com.example.umlandowallet.data.ConfirmedTx
import com.example.umlandowallet.data.Tx
import com.example.umlandowallet.data.TxResponse
import com.example.umlandowallet.data.TxStatus
import com.example.umlandowallet.toByteArray
import com.example.umlandowallet.toHex
import com.example.umlandowallet.ui.LogProgress
import org.bitcoindevkit.Blockchain
import org.bitcoindevkit.Progress
import org.bitcoindevkit.Wallet
import org.ldk.structs.ChainMonitor
import org.ldk.structs.ChannelManager
import org.ldk.structs.TwoTuple_usizeTransactionZ

class AccessImpl(
    private val blockchain: Blockchain,
) : Access {
    override suspend fun sync() {
        val currentHeight = blockchain.getHeight()
    }

    override suspend fun syncWallet(wallet: Wallet, logProgress: Progress) {
        wallet.sync(blockchain, LogProgress)
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
                val hexTx = service.getHexTx(txId)
                val (txByteArray, txJson) = service.getTx(txId)
                if (txJson.status.block_height != null) {
                    val blockHeader = service.getHeader(txJson.status.block_hash)
                    val merkleProof = service.getMerkleProof(txId)
                    if (txJson.status.block_height == merkleProof.block_height) {
                        confirmedTxs.add(ConfirmedTx(
                                tx = hexTx.toByteArray(),
                                block_height = txJson.status.block_height,
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