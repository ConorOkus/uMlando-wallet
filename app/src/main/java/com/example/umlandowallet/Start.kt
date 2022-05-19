package com.example.umlandowallet

import org.ldk.batteries.ChannelManagerConstructor
import org.ldk.enums.ConfirmationTarget
import org.ldk.enums.Level
import org.ldk.enums.Network
import org.ldk.impl.bindings
import org.ldk.structs.*
import org.ldk.structs.Logger.LoggerInterface
import org.ldk.structs.FeeEstimator.FeeEstimatorInterface
import org.ldk.structs.Persister.PersisterInterface
import java.io.File
import java.net.InetSocketAddress
import java.util.ArrayList

fun start(
    entropy: String,
    latestBlockHeight: Int,
    latestBlockHash: String,
    serializedChannelManager: String,
    serializedChannelMonitors: String
) {
    println("LDK starting...")

    // Estimating fees for on-chain transactions that LDK wants to broadcast.
    val feeEstimator: FeeEstimator = FeeEstimator.new_impl(LDKFeeEstimator)

    // LDK logging
    val logger: Logger = Logger.new_impl(LDKLogger)

    // Broadcasting various lightning transactions, including commitment transactions
    val txBroadcaster: BroadcasterInterface = BroadcasterInterface.new_impl(LDKBroadcaster)

    // Optional: Here we initialize the NetworkGraph so LDK provides routes for us
    val network : Network = Network.LDKNetwork_Regtest
    val genesisBlock : BestBlock = BestBlock.from_genesis(network)
    val genesisBlockHash : String = byteArrayToHex(genesisBlock.block_hash())

    initializeNetworkGraph(genesisBlockHash)

    // Persisting crucial channel data in a timely manner
    val persister: Persist = Persist.new_impl(LDKPersister)

    // Filter the transactions we want to monitor on chain
    val txFilter : Filter = Filter.new_impl(LDKTxFilter)
    val filter = Option_FilterZ.some(txFilter);

    // Monitor the chain for lighting transactions that are relevant to our
    // node, and broadcasting force close transactions if need be
    Global.chainMonitor = ChainMonitor.of(filter, txBroadcaster, logger, feeEstimator, persister)

    // Providing keys for signing lightning transactions
    Global.keysManager = KeysManager.of(
        entropy.hexStringToByteArray(),
        System.currentTimeMillis() / 1000,
        (System.currentTimeMillis() * 1000).toInt()
    )

    // Read Channel Monitor state from disk
    // Initialize the hashmap where we'll store the `ChannelMonitor`s read from disk.
    // This hashmap will later be given to the `ChannelManager` on initialization.
    var channelMonitors = arrayOf<ByteArray>();
    if (serializedChannelMonitors != "") {
        println("LDK: initiating channel monitors...");
        val channelMonitorHexes = serializedChannelMonitors.split(",").toTypedArray();
        val channelMonitorList = ArrayList<ByteArray>()
        channelMonitorHexes.iterator().forEach {
            val channelMonitorBytes = it.hexStringToByteArray();
            channelMonitorList.add(channelMonitorBytes);
        }
        channelMonitors = channelMonitorList.toTypedArray();
    }

   // Initialize the channel manager for managing channel state
    val scorer = MultiThreadedLockableScore.of(Scorer.with_default().as_Score())

    // This is going to be the fee policy for __incoming__ channels. they are set upfront globally:
    val userConfig = UserConfig.with_default()
    val newChannelConfig = ChannelConfig.with_default()
    newChannelConfig.set_forwarding_fee_proportional_millionths(10000);
    newChannelConfig.set_forwarding_fee_base_msat(1000);

    val handshake = ChannelHandshakeConfig.with_default();
    handshake.set_minimum_depth(1);
    userConfig.set_own_channel_config(handshake);

    userConfig.set_channel_options(newChannelConfig);
    val newLim = ChannelHandshakeLimits.with_default()
    newLim.set_force_announced_channel_preference(false)
    userConfig.set_peer_channel_config_limits(newLim)

    try {
        if (serializedChannelManager != "") {
            // loading from disk (restarting)
            Global.channelManagerConstructor = ChannelManagerConstructor(
                serializedChannelManager.hexStringToByteArray(),
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
            Global.channelManager = Global.channelManagerConstructor!!.channel_manager;
            Global.channelManagerConstructor!!.chain_sync_completed(ChannelManagerEventHandler, scorer);
            Global.peerManager = Global.channelManagerConstructor!!.peer_manager;
            Global.nioPeerHandler = Global.channelManagerConstructor!!.nio_peer_handler;
            Global.router = Global.channelManagerConstructor!!.net_graph;
        } else {
            // fresh start
            Global.channelManagerConstructor = ChannelManagerConstructor(
                Network.LDKNetwork_Regtest,
                userConfig,
                latestBlockHash.hexStringToByteArray(),
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

        // It seems that if you want to communicate from your computer to your emulator,
        // the IP address to use is 127.0.0.1 and you need to do some port forwarding
        // using ADB in command line e.g adb forward tcp:9777 tcp:9777
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
        println(record!!._args)
    }
}

// To create a transaction broadcaster we need provide an object that implements the BroadcasterInterface
// which has 1 function broadcast_transaction(tx: ByteArray?)
object LDKBroadcaster: BroadcasterInterface.BroadcasterInterfaceInterface {
    override fun broadcast_transaction(tx: ByteArray?) {
        println("Broadcasting transaction" + tx?.let { byteArrayToHex(it) })
    }
}

fun initializeNetworkGraph(genesisBlockHash: String) {
    val f = File(Global.homeDir+ "/" + Global.prefixNetworkGraph);
    if (f.exists()) {
        println("loading network graph...")
        val serializedGraph = File(Global.homeDir+ "/" + Global.prefixNetworkGraph).readBytes()
        val readResult = NetworkGraph.read(serializedGraph)
        if (readResult is Result_NetworkGraphDecodeErrorZ.Result_NetworkGraphDecodeErrorZ_OK) {
            Global.router = readResult.res
            println("loaded network graph ok")
        } else {
            println("network graph load failed")
            if (readResult is Result_NetworkGraphDecodeErrorZ.Result_NetworkGraphDecodeErrorZ_Err) {
                println(readResult.err);
            }

            // error, creating from scratch
            Global.router =
                NetworkGraph.of(genesisBlockHash.hexStringToByteArray().reversedArray())
        }
    } else {
        // first run, creating from scratch
        Global.router =
            NetworkGraph.of(genesisBlockHash.hexStringToByteArray().reversedArray())
    }
}


// To create a Perisister for our Channel Monitors we need to proved and object that implements the PersistInterface
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
        File(Global.homeDir + "/" + Global.prefixChannelMonitor + byteArrayToHex(id.write()) + ".hex").writeText(
            byteArrayToHex(channelMonitorBytes)
        );
        return Result_NoneChannelMonitorUpdateErrZ.ok();
    }

    override fun update_persisted_channel(
        id: OutPoint?,
        update: ChannelMonitorUpdate?,
        data: ChannelMonitor?,
        updateId: MonitorUpdateId
    ): Result_NoneChannelMonitorUpdateErrZ? {
        if (id == null || data == null) return null;
        val channelMonitorBytes = data.write()
        println("update_persisted_channel");
        File(Global.homeDir + "/" + Global.prefixChannelMonitor + byteArrayToHex(id.write()) + ".hex").writeText(
            byteArrayToHex(channelMonitorBytes)
        );
        return Result_NoneChannelMonitorUpdateErrZ.ok();
    }
}

// Responsible for backing up channel_manager bytes
object ChannelManagerEventHandler : ChannelManagerConstructor.EventHandler {
    override fun handle_event(event: Event) {
        handleEvent(event);
    }

    override fun persist_manager(channel_manager_bytes: ByteArray?) {
        println("persist_manager");
        if (channel_manager_bytes != null) {
            val hex = byteArrayToHex(channel_manager_bytes)
            println("channel_manager_bytes: $hex")
            File(Global.homeDir + "/" + Global.prefixChannelManager).writeText(byteArrayToHex(channel_manager_bytes));
        }
    }

    override fun persist_network_graph(network_graph: ByteArray?) {
        println("persist_network_graph");
        if(Global.prefixNetworkGraph != "" && network_graph !== null) {
            val hex = byteArrayToHex(network_graph)
            println("persist_network_graph_bytes: $hex");
            File(Global.homeDir + "/" + Global.prefixNetworkGraph).writeText(byteArrayToHex(network_graph))
        }
    }
}

// Filter allows LDK to let you know what transactions you should filter blocks for. This is
// useful if you pre-filter blocks or use compact filters. Otherwise, LDK will need full blocks.
object LDKTxFilter : Filter.FilterInterface {
    override fun register_tx(txid: ByteArray, script_pubkey: ByteArray) {
        println("register_tx");
        val params = WritableMap()
        params.putString("txid", byteArrayToHex(txid.reversedArray()))
        params.putString("script_pubkey", byteArrayToHex(script_pubkey))
        storeEvent(Global.homeDir + "/events_register_tx", params)
        Global.eventsRegisterTx = Global.eventsRegisterTx.plus(params.toString())
    }

    override fun register_output(output: WatchedOutput): Option_C2Tuple_usizeTransactionZZ {
        println("register_output");
        val params = WritableMap()
        val blockHash = output._block_hash;
        if (blockHash is ByteArray) {
            params.putString("block_hash", byteArrayToHex(blockHash))
        }
        params.putString("index", output._outpoint._index.toString())
        params.putString("script_pubkey", byteArrayToHex(output._script_pubkey))
        storeEvent(Global.homeDir + "/events_register_output", params)
        Global.eventsRegisterOutput = Global.eventsRegisterOutput.plus(params.toString())
        return Option_C2Tuple_usizeTransactionZZ.none();
    }
}
