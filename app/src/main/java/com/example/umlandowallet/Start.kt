package com.example.umlandowallet

import android.util.Log
import com.example.umlandowallet.utils.*
import org.ldk.batteries.ChannelManagerConstructor
import org.ldk.enums.Network
import org.ldk.structs.*
import java.io.File
import java.net.InetSocketAddress

fun start(
    entropy: ByteArray,
    latestBlockHeight: Int,
    latestBlockHash: String,
    serializedChannelManager: ByteArray?,
    serializedChannelMonitors: Array<ByteArray>
) {
    Log.i(LDKTAG, "LDK starting...")
    Log.i(
        LDKTAG,
        "This wallet is using the LDK Java bindings version ${org.ldk.impl.version.get_ldk_java_bindings_version()}"
    )

    // Estimating fees for on-chain transactions that LDK wants to broadcast.
    val feeEstimator: FeeEstimator = FeeEstimator.new_impl(LDKFeeEstimator)

    // LDK logging
    val logger: Logger = Logger.new_impl(LDKLogger)

    // Broadcasting various lightning transactions, including commitment transactions
    val txBroadcaster: BroadcasterInterface = BroadcasterInterface.new_impl(LDKBroadcaster)

    // Persisting crucial channel data in a timely manner
    val persister: Persist = Persist.new_impl(LDKPersister)

    // Filter the transactions we want to monitor on chain
    val txFilter: Filter = Filter.new_impl(LDKTxFilter)
    val filter = Option_FilterZ.some(txFilter)

    // Optional: Here we initialize the NetworkGraph so LDK does path finding and provides routes for us
    val f = File(Global.homeDir + "/" + "network-graph.bin")
    var networkGraph = NetworkGraph.of(Network.LDKNetwork_Regtest, logger)
    if (f.exists()) {
        Log.i(LDKTAG, "Loading network graph from: ${f.absolutePath}")
        val readResult = NetworkGraph.read(f.readBytes(), logger)
        if (readResult.is_ok) {
            networkGraph = (readResult as Result_NetworkGraphDecodeErrorZ.Result_NetworkGraphDecodeErrorZ_OK).res
        } else {
            Log.i(LDKTAG, "Failed to load cached network graph from disk. Will sync from scratch.")
            networkGraph = NetworkGraph.of(Network.LDKNetwork_Regtest, logger)
        }
    } else {
        Log.i(LDKTAG, "Failed to load cached network graph from disk. Will sync from scratch.")
        networkGraph = NetworkGraph.of(Network.LDKNetwork_Regtest, logger)
    }

    // Monitor the chain for lighting transactions that are relevant to our
    // node, and broadcasting force close transactions if need be
    Global.chainMonitor = ChainMonitor.of(filter, txBroadcaster, logger, feeEstimator, persister)

    // Providing keys for signing lightning transactions
    Global.keysManager = KeysManager.of(
        entropy,
        System.currentTimeMillis() / 1000,
        (System.currentTimeMillis() * 1000).toInt()
    )

    // This is going to be the fee policy for __incoming__ channels. they are set upfront globally:
    val userConfig = UserConfig.with_default()
    val newChannelConfig = ChannelConfig.with_default()
    newChannelConfig._forwarding_fee_proportional_millionths = 10000
    newChannelConfig._forwarding_fee_base_msat = 1000
    userConfig._channel_config = newChannelConfig


    val channelHandShakeConfig = ChannelHandshakeConfig.with_default()
    channelHandShakeConfig._minimum_depth = 1
    channelHandShakeConfig._announced_channel = false
    userConfig._channel_handshake_config = channelHandShakeConfig

    val channelHandshakeLimits = ChannelHandshakeLimits.with_default()
    channelHandshakeLimits._max_minimum_depth = 1
    userConfig._channel_handshake_limits = channelHandshakeLimits

    val probabilisticScorer = initialiseProbabilisticScorer(logger)
    val scorer = MultiThreadedLockableScore.of(probabilisticScorer!!.as_Score())
    val scoringParams = ProbabilisticScoringFeeParameters.with_default()

    try {
        if (serializedChannelManager != null) {
            // loading from disk (restarting)
            val channelManagerConstructor = ChannelManagerConstructor(
                Network.LDKNetwork_Regtest,
                userConfig,
                latestBlockHash.toByteArray(),
                latestBlockHeight,
                Global.keysManager!!.as_EntropySource(),
                Global.keysManager!!.as_NodeSigner(),
                Global.keysManager!!.as_SignerProvider(),
                feeEstimator,
                Global.chainMonitor,
                networkGraph,
                ProbabilisticScoringDecayParameters.with_default(),
                ProbabilisticScoringFeeParameters.with_default(),
                null,
                txBroadcaster,
                logger
            )

            Global.channelManagerConstructor = channelManagerConstructor
            Global.channelManager = channelManagerConstructor.channel_manager
            Global.nioPeerHandler = channelManagerConstructor.nio_peer_handler
            Global.peerManager = channelManagerConstructor.peer_manager
            Global.networkGraph = channelManagerConstructor.net_graph

            channelManagerConstructor.chain_sync_completed(
                LDKEventHandler,
                true
            )

            // If you want to communicate from your computer to your emulator,
            // the IP address to use is 127.0.0.1 and you need to do some port forwarding
            // using ADB in command line e.g adb forward tcp:9777 tcp:9777
            // If you want to do the reverse use 10.0.2.2 instead of localhost

            channelManagerConstructor.nio_peer_handler.bind_listener(InetSocketAddress("127.0.0.1", 9777))
        } else {
            // fresh start
            val channelManagerConstructor = ChannelManagerConstructor(
                Network.LDKNetwork_Regtest,
                userConfig,
                latestBlockHash.toByteArray(),
                latestBlockHeight,
                Global.keysManager!!.as_EntropySource(),
                Global.keysManager!!.as_NodeSigner(),
                Global.keysManager!!.as_SignerProvider(),
                feeEstimator,
                Global.chainMonitor,
                networkGraph,
                ProbabilisticScoringDecayParameters.with_default(),
                ProbabilisticScoringFeeParameters.with_default(),
                null,
                txBroadcaster,
                logger
            )

            Global.channelManagerConstructor = channelManagerConstructor
            Global.channelManager = channelManagerConstructor.channel_manager
            Global.peerManager = channelManagerConstructor.peer_manager
            Global.nioPeerHandler = channelManagerConstructor.nio_peer_handler
            networkGraph = channelManagerConstructor.net_graph
            channelManagerConstructor.chain_sync_completed(
                LDKEventHandler,
                true
            )

            channelManagerConstructor.nio_peer_handler.bind_listener(InetSocketAddress("127.0.0.1", 9777))
        }
    } catch (e: Exception) {
        Log.i(LDKTAG, "LDK: can't start, ${e.message}")
    }
}

fun initialiseProbabilisticScorer(logger: Logger): ProbabilisticScorer? {
    // Read Scorer data from disk
    val params = ProbabilisticScoringFeeParameters.with_default()

    val scorerFile = File("${Global.homeDir}/scorer.bin")
    if (scorerFile.exists()) {
        val read = ProbabilisticScorer.read(scorerFile.readBytes(), ProbabilisticScoringDecayParameters.with_default(), Global.router, logger)
        if (read.is_ok) {
            Log.i(LDKTAG, "Loaded scorer from disk")
            return (read as Result_ProbabilisticScorerDecodeErrorZ.Result_ProbabilisticScorerDecodeErrorZ_OK).res
        } else {
            Log.i(LDKTAG, "Failed to load cached scorer")
        }
    }

    val defaultScorer = ProbabilisticScorer.of(ProbabilisticScoringDecayParameters.with_default(), Global.router, logger)
    val scorerRes = ProbabilisticScorer.read(
        defaultScorer.write(), ProbabilisticScoringDecayParameters.with_default(), Global.router,
        logger
    )
    if (!scorerRes.is_ok) {
        return null
    }
    return (scorerRes as Result_ProbabilisticScorerDecodeErrorZ.Result_ProbabilisticScorerDecodeErrorZ_OK).res
}


