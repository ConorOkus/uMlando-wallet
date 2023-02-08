package com.example.umlandowallet.data.remote

import com.example.umlandowallet.data.OnchainWallet
import org.bitcoindevkit.*
import org.ldk.structs.ChainMonitor
import org.ldk.structs.ChannelManager
import org.ldk.structs.TwoTuple_TxidBlockHashZ

interface Access {
    suspend fun sync(): Unit

    suspend fun syncWallet(onchainWallet: OnchainWallet): Unit

    suspend fun syncBestBlockConnected(
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    ): Unit

    suspend fun syncTransactionConfirmed(
        relevantTxIds: Array<TwoTuple_TxidBlockHashZ>,
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    ): Unit

    suspend fun syncTransactionsUnconfirmed(
        relevantTxIds: Array<TwoTuple_TxidBlockHashZ>,
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    ): Unit

    companion object {
        fun create(): Access {
            return AccessImpl()
        }
    }
}