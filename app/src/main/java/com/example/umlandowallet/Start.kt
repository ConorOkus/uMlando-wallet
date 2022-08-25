package com.example.umlandowallet

import com.example.umlandowallet.data.remote.Service
import com.sun.tools.javac.util.ArrayUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.ldk.batteries.ChannelManagerConstructor
import org.ldk.enums.ConfirmationTarget
import org.ldk.enums.Network
import org.ldk.structs.*
import org.ldk.structs.FeeEstimator.FeeEstimatorInterface
import org.ldk.structs.Logger.LoggerInterface
import java.io.File
import java.net.InetSocketAddress


fun start(
    entropy: String,
    latestBlockHeight: Int,
    latestBlockHash: String,
    serializedChannelManager: String,
    serializedChannelMonitors: String
) {
    println("LDK starting...")

    println(org.ldk.impl.version.get_ldk_java_bindings_version())

    // Estimating fees for on-chain transactions that LDK wants to broadcast.
    val feeEstimator: FeeEstimator = FeeEstimator.new_impl(LDKFeeEstimator)

    // LDK logging
    val logger: Logger = Logger.new_impl(LDKLogger)

    // Broadcasting various lightning transactions, including commitment transactions
    val txBroadcaster: BroadcasterInterface = BroadcasterInterface.new_impl(LDKBroadcaster)

    // Optional: Here we initialize the NetworkGraph so LDK does path finding and provides routes for us
    val network : Network = Network.LDKNetwork_Testnet
    val genesisBlock : BestBlock = BestBlock.from_genesis(network)
    val genesisBlockHash : ByteArray = genesisBlock.block_hash()

    initializeNetworkGraph(genesisBlockHash, logger)

    // Persisting crucial channel data in a timely manner
    val persister: Persist = Persist.new_impl(LDKPersister)

    // Filter the transactions we want to monitor on chain
    val txFilter : Filter = Filter.new_impl(LDKTxFilter)
    val filter = Option_FilterZ.some(txFilter)

    // Monitor the chain for lighting transactions that are relevant to our
    // node, and broadcasting force close transactions if need be
    Global.chainMonitor = ChainMonitor.of(filter, txBroadcaster, logger, feeEstimator, persister)

    // Providing keys for signing lightning transactions
    Global.keysManager = KeysManager.of(
        entropy.toByteArray(),
        System.currentTimeMillis() / 1000,
        (System.currentTimeMillis() * 1000).toInt()
    )

    // Read Channel Monitor state from disk
    // Initialize the hashmap where we'll store the `ChannelMonitor`s read from disk.
    // This hashmap will later be given to the `ChannelManager` on initialization.
    var channelMonitors = arrayOf<ByteArray>()
    if (serializedChannelMonitors != "") {
        println("LDK: initiating channel monitors...")
        val channelMonitorHexes = serializedChannelMonitors.split(",").toTypedArray();
        val channelMonitorList = ArrayList<ByteArray>()
        channelMonitorHexes.iterator().forEach {
            val channelMonitorBytes = it.toByteArray()
            channelMonitorList.add(channelMonitorBytes)
        }
        channelMonitors = channelMonitorList.toTypedArray()

//        val list = Global.chainMonitor!!.list_monitors()
//        list.iterator().forEach { outPoint ->
//            val monitor  = // Retrieve channel monitor saved in step 4
//            Global.chainMonitor!!.as_Watch().watch_channel(outPoint, monitor)
//        }
    }

    // This is going to be the fee policy for __incoming__ channels. they are set upfront globally:
    val userConfig = UserConfig.with_default()
    val newChannelConfig = ChannelConfig.with_default()
    newChannelConfig.set_forwarding_fee_proportional_millionths(10000)
    newChannelConfig.set_forwarding_fee_base_msat(1000)
    userConfig.set_channel_config(newChannelConfig)


    val handshake = ChannelHandshakeConfig.with_default()
    handshake.set_minimum_depth(1)
    userConfig.set_channel_handshake_config(handshake)

    val newLim = ChannelHandshakeLimits.with_default()
    newLim.set_force_announced_channel_preference(false)
    userConfig.set_channel_handshake_limits(newLim)

    val params = ProbabilisticScoringParameters.with_default()
    val defaultScorer = ProbabilisticScorer.of(params, Global.router, logger).as_Score()
    val scorer = MultiThreadedLockableScore.of(defaultScorer)

    try {
        if (serializedChannelManager != "") {
            // loading from disk (restarting)
            Global.channelManagerConstructor = ChannelManagerConstructor(
                serializedChannelManager.toByteArray(),
                channelMonitors,
                userConfig,
                Global.keysManager?.as_KeysInterface(),
                feeEstimator,
                Global.chainMonitor,
                Global.txFilter,
                Global.router!!.write(),
                txBroadcaster,
                logger
            );

            Global.channelManager = Global.channelManagerConstructor!!.channel_manager

            val relevant_txids_1: Array<ByteArray> = Global.channelManager!!.as_Confirm().get_relevant_txids()
            println("relevant_txids_1: ${relevant_txids_1}")
            val relevant_txids_2: Array<ByteArray> = Global.channelManager!!.as_Confirm().get_relevant_txids()
            println("relevant_txids_2: ${relevant_txids_2}")

            val relevant_txids: Array<ByteArray> = relevant_txids_1 + relevant_txids_2
            
            Global.channelManagerConstructor!!.chain_sync_completed(ChannelManagerEventHandler, scorer);
            Global.peerManager = Global.channelManagerConstructor!!.peer_manager;
            Global.nioPeerHandler = Global.channelManagerConstructor!!.nio_peer_handler;
            Global.router = Global.channelManagerConstructor!!.net_graph;
        } else {
            // fresh start
            Global.channelManagerConstructor = ChannelManagerConstructor(
                Network.LDKNetwork_Testnet,
                userConfig,
                latestBlockHash.toByteArray(),
                latestBlockHeight,
                Global.keysManager?.as_KeysInterface(),
                feeEstimator,
                Global.chainMonitor,
                Global.router,
                txBroadcaster,
                logger
            );
            Global.channelManager = Global.channelManagerConstructor!!.channel_manager;
            Global.channelManagerConstructor!!.chain_sync_completed(ChannelManagerEventHandler, scorer);
            Global.peerManager = Global.channelManagerConstructor!!.peer_manager;
            Global.nioPeerHandler = Global.channelManagerConstructor!!.nio_peer_handler;
            Global.router = Global.channelManagerConstructor!!.net_graph;
        }

        // If you want to communicate from your computer to your emulator,
        // the IP address to use is 127.0.0.1 and you need to do some port forwarding
        // using ADB in command line e.g adb forward tcp:9777 tcp:9777
        // If you want to do the reverse use 10.0.2.2 instead of localhost
        Global.nioPeerHandler!!.bind_listener(InetSocketAddress("127.0.0.1", 9777))
    } catch (e: Exception) {
        println("LDK: can't start, " + e.message);
    }
}

// To create a FeeEstimator we need to provide an object that implements the FeeEstimatorInterface
// which has 1 function: get_est_sat_per_1000_weight(conf_target: ConfirmationTarget?): Int
object LDKFeeEstimator: FeeEstimatorInterface {
    override fun get_est_sat_per_1000_weight(conf_target: ConfirmationTarget?): Int {
        return Global.feerateFast
    }
}

// To create a Logger we need to provide an object that implements the LoggerInterface
// which has 1 function: log(record: Record?): Unit
object LDKLogger : LoggerInterface {
    override fun log(record: Record?) {
//        println(record!!)
    }
}

// To create a transaction broadcaster we need provide an object that implements the BroadcasterInterface
// which has 1 function broadcast_transaction(tx: ByteArray?)
object LDKBroadcaster: BroadcasterInterface.BroadcasterInterfaceInterface {
    override fun broadcast_transaction(tx: ByteArray?): Unit {
        val service = Service.create()

        tx?.let {
            GlobalScope.launch {
                val txid: String = service.broadcastTx(tx)
                println("We've broadcast a transaction with txid $txid")
            }
        } ?: throw(IllegalStateException("Broadcaster attempted to broadcast an null transaction"))
    }
}

fun initializeNetworkGraph(genesisBlockHash: ByteArray, logger: Logger) {
    val f = File(Global.homeDir+ "/" + Global.prefixNetworkGraph);

    if (f.exists()) {
        println("loading network graph from: ${Global.homeDir + "/" + Global.prefixNetworkGraph}")
        val serializedGraph = File(Global.homeDir + "/" + Global.prefixNetworkGraph).readBytes()
        val readResult = NetworkGraph.read(serializedGraph, logger)
        println("readResult: ${readResult}")

        if (readResult is Result_NetworkGraphDecodeErrorZ.Result_NetworkGraphDecodeErrorZ_OK) {
            Global.router = readResult.res
            Global.p2pGossipSync = P2PGossipSync.of(readResult.res, Option_AccessZ.none(), logger)
            println("loaded network graph ok")
        } else {
            println("network graph load failed")
            if (readResult is Result_NetworkGraphDecodeErrorZ.Result_NetworkGraphDecodeErrorZ_Err) {
                println(readResult.err);
            }

            // error, creating from scratch
            Global.router =
                NetworkGraph.of(genesisBlockHash.reversedArray(), logger)
        }
    } else {
        // first run, creating from scratch
        Global.router =
            NetworkGraph.of(genesisBlockHash.reversedArray(), logger)
    }
}


// To create a Perisister for our Channel Monitors we need to provide an object that implements the PersistInterface
// which has 2 functions persist_new_channel & update_persisted_channel
object LDKPersister: Persist.PersistInterface {
    override fun persist_new_channel(
        id: OutPoint?,
        data: ChannelMonitor?,
        updateId: MonitorUpdateId?
    ): Result_NoneChannelMonitorUpdateErrZ? {
        if (id == null || data == null) return null;
        val channelMonitorBytes = data.write()
        println("persist_new_channel")
        File(Global.homeDir + "/" + Global.prefixChannelMonitor + id.write().toHex() + ".hex").writeText(
            channelMonitorBytes.toHex()
        );
        // Save MonitorUpdateId so it can be used in channel_monitor_updated
        return Result_NoneChannelMonitorUpdateErrZ.ok();
    }

    override fun update_persisted_channel(
        id: OutPoint?,
        update: ChannelMonitorUpdate?,
        data: ChannelMonitor?,
        updateId: MonitorUpdateId
    ): Result_NoneChannelMonitorUpdateErrZ? {
        if (id == null || data == null || update == null) return null;
        val channelMonitorBytes = update.write()
        println("update_persisted_channel");
        File(Global.homeDir + "/" + Global.prefixChannelMonitor + id.write().toHex() + ".hex").writeText(
            channelMonitorBytes.toHex()
        );
        // Save MonitorUpdateId so it can be used in channel_monitor_updated
        return Result_NoneChannelMonitorUpdateErrZ.ok();
    }
}

// Responsible for backing up channel_manager bytes
object ChannelManagerEventHandler : ChannelManagerConstructor.EventHandler {
    override fun handle_event(event: Event) {
        println("Getting ready to handle event")
        handleEvent(event);
    }

    override fun persist_manager(channel_manager_bytes: ByteArray?) {
        println("persist_manager");
        if (channel_manager_bytes != null) {
            val hex = channel_manager_bytes.toHex()
            println("channel_manager_bytes: $hex")
            File(Global.homeDir + "/" + Global.prefixChannelManager).writeText(channel_manager_bytes.toHex());
        }
    }

    override fun persist_network_graph(network_graph: ByteArray?) {
        println("persist_network_graph");
        if(Global.prefixNetworkGraph != "" && network_graph !== null) {
            val hex = network_graph.toHex()
            println("persist_network_graph_bytes: $hex");
            File(Global.homeDir + "/" + Global.prefixNetworkGraph).writeText(network_graph.toHex())
        }
    }

    override fun persist_scorer(scorer: ByteArray?) {
        println("scorer");
        if(Global.prefixScorer != "" && scorer !== null) {
            val hex = scorer.toHex()
            println("scorer_bytes: $hex");
            File(Global.homeDir + "/" + Global.prefixScorer).writeText(scorer.toHex())
        }
    }
}

// Filter allows LDK to let you know what transactions you should filter blocks for. This is
// useful if you pre-filter blocks or use compact filters. Otherwise, LDK will need full blocks.
object LDKTxFilter : Filter.FilterInterface {
    override fun register_tx(txid: ByteArray, script_pubkey: ByteArray) {
        println("register_tx");
        val params = WritableMap()
        params.putString("txid", txid.reversedArray().toHex())
        params.putString("script_pubkey", script_pubkey.toHex())
        storeEvent(Global.homeDir + "/events_register_tx", params)
        Global.eventsRegisterTx = Global.eventsRegisterTx.plus(params.toString())
    }

    override fun register_output(output: WatchedOutput): Option_C2Tuple_usizeTransactionZZ {
        println("register_output");
        val params = WritableMap()
        val blockHash = output._block_hash;
        if (blockHash is ByteArray) {
            params.putString("block_hash", blockHash.toHex())
        }
        params.putString("index", output._outpoint._index.toString())
        params.putString("script_pubkey", output._script_pubkey.toHex())
        storeEvent(Global.homeDir + "/events_register_output", params)
        Global.eventsRegisterOutput = Global.eventsRegisterOutput.plus(params.toString())
        return Option_C2Tuple_usizeTransactionZZ.none();
    }
}
