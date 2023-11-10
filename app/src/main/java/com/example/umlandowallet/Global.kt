package com.example.umlandowallet

import com.example.umlandowallet.data.WatchedTransaction
import org.ldk.batteries.ChannelManagerConstructor
import org.ldk.batteries.NioPeerHandler
import org.ldk.structs.*

object Global {
    @JvmField
    var homeDir: String = ""

    var eventsTxBroadcast: Array<String> = arrayOf<String>()
    var eventsPaymentSent: Array<String> = arrayOf<String>()
    var eventsPaymentPathFailed: Array<String> = arrayOf<String>()
    var eventsPaymentFailed: Array<String> = arrayOf<String>()
    var eventsPaymentReceived: Array<String> = arrayOf<String>()
    var eventsFundingGenerationReady: Array<String> = arrayOf<String>()
    var eventsPaymentForwarded: Array<String> = arrayOf<String>()
    var eventsChannelClosed: Array<String> = arrayOf<String>()
    var eventsRegisterTx: Array<String> = arrayOf<String>()
    var eventsRegisterOutput: Array<String> = arrayOf<String>()

    var refundAddressScript = ""

    var channelManager: ChannelManager? = null
    var keysManager: KeysManager? = null
    var chainMonitor: ChainMonitor? = null
    var temporaryChannelId: ByteArray? = null
    var counterpartyNodeId: ByteArray? = null
    var channelManagerConstructor: ChannelManagerConstructor? = null
    var nioPeerHandler: NioPeerHandler? = null
    var peerManager: PeerManager? = null

    var networkGraph: NetworkGraph? = null
    var p2pGossipSync: P2PGossipSync? = null
    var txFilter: Filter? = null
    var scorer: MultiThreadedLockableScore? = null
    val relevantTxs: ArrayList<WatchedTransaction> = arrayListOf()
    val relevantOutputs: ArrayList<WatchedOutput> = arrayListOf()
}