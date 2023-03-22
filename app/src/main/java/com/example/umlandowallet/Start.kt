package com.example.umlandowallet

import android.util.Log
import com.example.umlandowallet.data.WatchedTransaction
import com.example.umlandowallet.data.remote.Service
import com.example.umlandowallet.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bitcoindevkit.Address
import org.bitcoindevkit.Script
import org.bitcoindevkit.Transaction
import org.ldk.batteries.ChannelManagerConstructor
import org.ldk.enums.ChannelMonitorUpdateStatus
import org.ldk.enums.ConfirmationTarget
import org.ldk.enums.Network
import org.ldk.enums.Recipient
import org.ldk.structs.*
import org.ldk.structs.FeeEstimator.FeeEstimatorInterface
import org.ldk.structs.Logger.LoggerInterface
import org.ldk.structs.KeysInterface.KeysInterfaceInterface
import org.ldk.util.UInt128
import org.ldk.util.UInt5
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

    // Optional: Here we initialize the NetworkGraph so LDK does path finding and provides routes for us
    val network: Network = Network.LDKNetwork_Regtest
    val genesisBlock: BestBlock = BestBlock.from_genesis(network)
    val genesisBlockHash: ByteArray = genesisBlock.block_hash()

    initializeNetworkGraph(genesisBlockHash, logger)

    // Persisting crucial channel data in a timely manner
    val persister: Persist = Persist.new_impl(LDKPersister)

    // Filter the transactions we want to monitor on chain
    val txFilter: Filter = Filter.new_impl(LDKTxFilter)
    val filter = Option_FilterZ.some(txFilter)

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

    val params = ProbabilisticScoringParameters.with_default()
    val defaultScorer = ProbabilisticScorer.of(params, Global.router, logger)
    val scoreRes = ProbabilisticScorer.read(
        defaultScorer.write(), params, Global.router,
        logger
    )
    if (!scoreRes.is_ok) {
        Log.i(LDKTAG, "Initialising scoring failed")
    }

    val score = (scoreRes as Result_ProbabilisticScorerDecodeErrorZ.Result_ProbabilisticScorerDecodeErrorZ_OK).res.as_Score()
    val scorer = MultiThreadedLockableScore.of(score)

    try {
        if (serializedChannelManager != null) {
            // loading from disk (restarting)
            val channelManagerConstructor = ChannelManagerConstructor(
                serializedChannelManager,
                serializedChannelMonitors,
                userConfig,
                Global.keysManager?.as_KeysInterface(),
                feeEstimator,
                Global.chainMonitor,
                Global.txFilter,
                Global.router!!.write(),
                txBroadcaster,
                logger
            )


            Global.channelManagerConstructor = channelManagerConstructor
            Global.channelManager = channelManagerConstructor.channel_manager
            Global.nioPeerHandler = channelManagerConstructor.nio_peer_handler
            Global.peerManager = channelManagerConstructor.peer_manager
            Global.router = channelManagerConstructor.net_graph
            Global.invoicePayer = channelManagerConstructor.payer
            Global.scorer = scorer

            channelManagerConstructor.chain_sync_completed(
                ChannelManagerEventHandler,
                scorer
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
                Global.keysManager?.as_KeysInterface(),
                feeEstimator,
                Global.chainMonitor,
                Global.router,
                txBroadcaster,
                logger
            )

            Global.channelManagerConstructor = channelManagerConstructor
            Global.channelManager = channelManagerConstructor.channel_manager
            Global.peerManager = channelManagerConstructor.peer_manager
            Global.nioPeerHandler = channelManagerConstructor.nio_peer_handler
            Global.router = channelManagerConstructor.net_graph
            Global.scorer = scorer
            Global.invoicePayer = channelManagerConstructor.payer
            channelManagerConstructor.chain_sync_completed(
                ChannelManagerEventHandler,
                scorer
            )

            channelManagerConstructor.nio_peer_handler.bind_listener(InetSocketAddress("127.0.0.1", 9777))
        }
    } catch (e: Exception) {
        Log.i(LDKTAG, "LDK: can't start, ${e.message}")
    }
}

// To create a FeeEstimator we need to provide an object that implements the FeeEstimatorInterface
// which has 1 function: get_est_sat_per_1000_weight(conf_target: ConfirmationTarget?): Int
object LDKFeeEstimator : FeeEstimatorInterface {
    override fun get_est_sat_per_1000_weight(confirmationTarget: ConfirmationTarget?): Int {
        if (confirmationTarget == ConfirmationTarget.LDKConfirmationTarget_Background) {
            return 12500
        }

        if (confirmationTarget == ConfirmationTarget.LDKConfirmationTarget_Normal) {
            return 12500
        }

        if (confirmationTarget == ConfirmationTarget.LDKConfirmationTarget_HighPriority) {
            return 12500
        }

        return 12500
    }
}

// To create a Logger we need to provide an object that implements the LoggerInterface
// which has 1 function: log(record: Record?): Unit
object LDKLogger : LoggerInterface {
    override fun log(record: Record?) {
        val rawLog = record!!._args.toString()
        Log.i(LDKTAG, rawLog)
    }
}

// To create a transaction broadcaster we need provide an object that implements the BroadcasterInterface
// which has 1 function broadcast_transaction(tx: ByteArray?)
object LDKBroadcaster : BroadcasterInterface.BroadcasterInterfaceInterface {
    override fun broadcast_transaction(tx: ByteArray?) {
        tx?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val uByteArray = UByteArray(tx.size) { tx[it].toUByte() }
                val transaction = Transaction(uByteArray.toList())

                OnchainWallet.broadcastRawTx(transaction)
                Log.i(LDKTAG, "The raw transaction broadcast is: ${tx.toHex()}")
            }
        } ?: throw(IllegalStateException("Broadcaster attempted to broadcast a null transaction"))
    }
}

fun initializeNetworkGraph(genesisBlockHash: ByteArray, logger: Logger) {
    if (Global.router !== null) {
        Log.i(LDKTAG, "Network graph already initialised")
    }
    val f = File(Global.homeDir + "/" + "network-graph.bin")

    if (f.exists()) {
        Log.i(LDKTAG, "Loading network graph from: ${f.absolutePath}")
        (NetworkGraph.read(f.readBytes(), logger) as? Result_NetworkGraphDecodeErrorZ.Result_NetworkGraphDecodeErrorZ_OK)?.let { res ->
            Log.i(LDKTAG, "Loaded network graph bytes")

            Global.router = res.res
        }
    }

    if (Global.router == null) {
        Log.i(LDKTAG, "Failed to load cached network graph from disk. Will sync from scratch.")
        Global.router = NetworkGraph.of(genesisBlockHash.reversedArray(), logger)
    }
}


// To create a Persister for our Channel Monitors we need to provide an object that implements the PersistInterface
// which has 2 functions persist_new_channel & update_persisted_channel
// Consider return ChannelMonitorUpdateStatus::InProgress for async backups
object LDKPersister : Persist.PersistInterface {
    private fun persist(id: OutPoint?, data: ByteArray?) {
        if(id != null && data != null) {
            val identifier = "channels/${id.to_channel_id().toHex()}.bin"
            write(identifier, data)
        }
    }

    override fun persist_new_channel(
        id: OutPoint?,
        data: ChannelMonitor?,
        updateId: MonitorUpdateId?
    ): ChannelMonitorUpdateStatus? {
        return try {
            if (data != null && id != null) {
                Log.i(LDKTAG, "persist_new_channel: ${id.to_channel_id().toHex()}")
                persist(id, data.write())
            }
            ChannelMonitorUpdateStatus.LDKChannelMonitorUpdateStatus_Completed
        } catch (e: Exception) {
            Log.i(LDKTAG, "Failed to write to file: ${e.message}")
            ChannelMonitorUpdateStatus.LDKChannelMonitorUpdateStatus_PermanentFailure
        }
    }

    // Consider returning ChannelMonitorUpdateStatus::InProgress for async backups
    override fun update_persisted_channel(
        id: OutPoint?,
        update: ChannelMonitorUpdate?,
        data: ChannelMonitor?,
        updateId: MonitorUpdateId
    ): ChannelMonitorUpdateStatus? {
        return try {
            if (data != null && id != null) {
                Log.i(LDKTAG, "update_persisted_channel: ${id.to_channel_id().toHex()}")
                persist(id, data.write())
            }
            ChannelMonitorUpdateStatus.LDKChannelMonitorUpdateStatus_Completed
        } catch (e: Exception) {
            Log.i(LDKTAG, "Failed to write to file: ${e.message}")
            ChannelMonitorUpdateStatus.LDKChannelMonitorUpdateStatus_PermanentFailure

        }

    }
}

// Responsible for backing up channel_manager bytes
object ChannelManagerEventHandler : ChannelManagerConstructor.EventHandler {
    override fun handle_event(event: Event) {
        Log.i(LDKTAG, "Getting ready to handle event")
        handleEvent(event)
    }

    override fun persist_manager(channelManagerBytes: ByteArray?) {
        if (channelManagerBytes != null) {
            Log.i(LDKTAG, "persist_manager")
            val identifier = "channel-manager.bin"
            write(identifier, channelManagerBytes)
        }
    }

    override fun persist_network_graph(networkGraph: ByteArray?) {
        if (networkGraph !== null) {
            Log.i(LDKTAG, "persist_network_graph")
            val identifier = "network-graph.bin"
            write(identifier, networkGraph)
        }
    }

    override fun persist_scorer(scorer: ByteArray?) {
        if (scorer !== null) {
            Log.i(LDKTAG, "persist_scorer")
            val identifier = "scorer.bin"
            write(identifier, scorer)
        }
    }
}

// Filter allows LDK to let you know what transactions you should filter blocks for. This is
// useful if you pre-filter blocks or use compact filters. Otherwise, LDK will need full blocks.
object LDKTxFilter : Filter.FilterInterface {
    override fun register_tx(txid: ByteArray, script_pubkey: ByteArray) {
        Log.i(LDKTAG, "register_tx")

        val txId = txid.reversedArray().toHex()
        val scriptPubkey = script_pubkey.toHex()

        val params = WritableMap()
        params.putString("txid", txId)
        params.putString("script_pubkey", scriptPubkey)
        storeEvent(Global.homeDir + "/events_register_tx", params)

        Global.eventsRegisterTx = Global.eventsRegisterTx.plus(params.toString())
        Global.relevantTxs.add(WatchedTransaction(txid, script_pubkey))

        Log.i(LDKTAG, Global.relevantTxs.toString())
    }

    override fun register_output(output: WatchedOutput) {
        Log.i(LDKTAG, "register_output")

        val index = output._outpoint._index.toString()
        val scriptPubkey = output._script_pubkey.toHex()

        val params = WritableMap()
        val blockHash = output._block_hash
        if (blockHash is ByteArray) {
            params.putString("block_hash", blockHash.toHex())
        }
        params.putString("index", index)
        params.putString("script_pubkey", scriptPubkey)

        storeEvent(Global.homeDir + "/events_register_output", params)

        Global.eventsRegisterOutput = Global.eventsRegisterOutput.plus(params.toString())
        Global.relevantOutputs.add(
            WatchedOutput.of(
                output._block_hash,
                output._outpoint,
                output._script_pubkey
            )
        )

        Log.i(LDKTAG, Global.relevantOutputs.toString())
    }
}
