package com.example.umlandowallet.data.remote

import com.example.umlandowallet.data.Tx
import com.example.umlandowallet.toByteArray
import com.example.umlandowallet.toHex
import com.example.umlandowallet.ui.LogProgress
import org.bitcoindevkit.Blockchain
import org.bitcoindevkit.Progress
import org.bitcoindevkit.Wallet
import org.ldk.structs.ChainMonitor
import org.ldk.structs.ChannelManager

class AccessImpl(
    private val blockchain: Blockchain,
) : Access {
    override suspend fun syncWallet(wallet: Wallet, logProgress: Progress) {
        wallet.sync(blockchain, LogProgress)
    }

    override suspend fun sync() {
        val currentHeight = blockchain.getHeight()
    }

    override suspend fun syncTransactionsUnconfirmed(
        relevantTxIds: Array<ByteArray>,
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    ) {
        val service = Service.create()

        for (txid in relevantTxIds) {
            val txId = txid.reversedArray().toHex()
            val response: Tx = service.getStatus(txId)
            if (!response.status.confirmed) {
                channelManager.as_Confirm().transaction_unconfirmed(txId.toByteArray())
                chainMonitor.as_Confirm().transaction_unconfirmed(txId.toByteArray())
            }
        }
    }
}