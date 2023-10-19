package com.example.umlandowallet.data.remote

import com.example.umlandowallet.OnchainWallet
import org.ldk.structs.ChainMonitor
import org.ldk.structs.ChannelManager
import org.ldk.structs.TwoTuple_ThirtyTwoBytesCOption_ThirtyTwoBytesZZ

interface Access {
    suspend fun sync()

    suspend fun syncWallet(onchainWallet: OnchainWallet)

    suspend fun syncBestBlockConnected(
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    )

    suspend fun syncTransactionConfirmed(
        relevantTxIds: Array<TwoTuple_ThirtyTwoBytesCOption_ThirtyTwoBytesZZ>,
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    )

    suspend fun syncTransactionsUnconfirmed(
        relevantTxIds: Array<TwoTuple_ThirtyTwoBytesCOption_ThirtyTwoBytesZZ>,
        channelManager: ChannelManager,
        chainMonitor: ChainMonitor
    )
}