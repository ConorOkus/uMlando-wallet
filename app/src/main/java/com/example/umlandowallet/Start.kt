package com.example.umlandowallet

import org.ldk.batteries.ChannelManagerConstructor
import org.ldk.enums.ConfirmationTarget
import org.ldk.enums.Level
import org.ldk.enums.Network
import org.ldk.structs.*
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
    println("LATEST BLOCK HASH: $latestBlockHash")
    println("LATEST BLOCK HEIGHT: $latestBlockHeight")


    // Initialize the FeeEstimator #################################################################
    // What it's used for: estimating fees for on-chain transactions that LDK wants to broadcast.
    val fee_estimator = FeeEstimator.new_impl { confirmation_target: ConfirmationTarget? ->
        var ret = Global.feerateFast
        if (confirmation_target != null) {
            if (confirmation_target.equals(ConfirmationTarget.LDKConfirmationTarget_HighPriority)) ret = Global.feerateFast
            if (confirmation_target.equals(ConfirmationTarget.LDKConfirmationTarget_Normal)) ret = Global.feerateMedium
            if (confirmation_target.equals(ConfirmationTarget.LDKConfirmationTarget_Background)) ret = Global.feerateSlow
        }
        return@new_impl ret;
    }

    // Initialize the Logger #######################################################################
    // What it's used for: LDK logging
    val logger = Logger.new_impl { arg: Record ->
        if (arg._level == Level.LDKLevel_Gossip) return@new_impl;
        if (arg._level == Level.LDKLevel_Trace) return@new_impl;
        if (arg._level == Level.LDKLevel_Debug) return@new_impl;
        println("Logger: " + arg._args)
        return@new_impl

//       val params = Arguments.createMap()
//       params.putString("line", arg)
//       sendEvent(MARKER_LOG, params)
    }

    // Initialize the Broadcaster #########################################################
    // What it's used for: broadcasting various lightning transactions, including commitment transactions
    val tx_broadcaster = BroadcasterInterface.new_impl { tx ->
        println("Broadcaster: " + "broadcaster sends an event asking to broadcast some txhex...")
        val params = WritableMap();
        params.putString("txhex", byteArrayToHex(tx))
        storeEvent(Global.homeDir + "/events_tx_broadcast", params)
        Global.eventsTxBroadcast = Global.eventsTxBroadcast.plus(params.toString())
    }

    // Initialize Persist ##########################################################################
    // What it's used for: persisting crucial channel data in a timely manner
    val persister = Persist.new_impl(object : Persist.PersistInterface {
        override fun persist_new_channel(
            id: OutPoint?,
            data: ChannelMonitor?,
            update_id: MonitorUpdateId?
        ): Result_NoneChannelMonitorUpdateErrZ? {
            if (id == null || data == null) return null;
            val channel_monitor_bytes = data.write()
            println("persist_new_channel")
            File(Global.homeDir + "/" + Global.prefixChannelMonitor + byteArrayToHex(id.write()) + ".hex").writeText(
                byteArrayToHex(channel_monitor_bytes)
            );
            return Result_NoneChannelMonitorUpdateErrZ.ok();
        }

        override fun update_persisted_channel(
            id: OutPoint?,
            update: ChannelMonitorUpdate?,
            data: ChannelMonitor?,
            update_id: MonitorUpdateId
        ): Result_NoneChannelMonitorUpdateErrZ? {
            if (id == null || data == null) return null;
            val channel_monitor_bytes = data.write()
            println("update_persisted_channel");
            File(Global.homeDir + "/" + Global.prefixChannelMonitor + byteArrayToHex(id.write()) + ".hex").writeText(
                byteArrayToHex(channel_monitor_bytes)
            );
            return Result_NoneChannelMonitorUpdateErrZ.ok();
        }
    })

    // Initializing channel manager persister that is responsible for backing up channel_manager bytes
    val channel_manager_persister = object : ChannelManagerConstructor.EventHandler {
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

    // Initialize Chain Monitor #################################################################
    // What it's used for: monitoring the chain for lighting transactions that are relevant to our
    // node, and broadcasting force close transactions if need be

    // Filter allows LDK to let you know what transactions you should filter blocks for. This is
    // useful if you pre-filter blocks or use compact filters. Otherwise, LDK will need full blocks.
    Global.txFilter = Filter.new_impl(object : Filter.FilterInterface {
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
    })

    val filter = Option_FilterZ.some(Global.txFilter);
    println(org.ldk.impl.version.get_ldk_java_bindings_version() + ", " + org.ldk.impl.bindings.get_ldk_c_bindings_version() + ", " + org.ldk.impl.bindings.get_ldk_version());

    Global.chainMonitor = ChainMonitor.of(filter, tx_broadcaster, logger, fee_estimator, persister)

    // Initialize the Keys Manager ##################################################################
    // What it's used for: providing keys for signing lightning transactions
    Global.keysManager = KeysManager.of(
        hexStringToByteArray(entropy),
        System.currentTimeMillis() / 1000,
        (System.currentTimeMillis() * 1000).toInt()
    )

    // Read Channel Monitor state from disk #########################################################
    // Initialize the hashmap where we'll store the `ChannelMonitor`s read from disk.
    // This hashmap will later be given to the `ChannelManager` on initialization.
    var channelMonitors = arrayOf<ByteArray>();
    if (serializedChannelMonitors != "") {
        println("LDK: initiating channel monitors...");
        val channelMonitorHexes = serializedChannelMonitors.split(",").toTypedArray();
        val channel_monitor_list = ArrayList<ByteArray>()
        channelMonitorHexes.iterator().forEach {
            val channel_monitor_bytes = hexStringToByteArray(it);
            channel_monitor_list.add(channel_monitor_bytes);
        }
        channelMonitors = channel_monitor_list.toTypedArray();
    }


    // Initialize Graph Sync #########################################################################
    val f = File(Global.homeDir+ "/" + Global.prefixNetworkGraph);
    if (f.exists()) {
        println("loading network graph...")
        val serialized_graph = File(Global.homeDir+ "/" + Global.prefixNetworkGraph).readBytes()
        val readResult = NetworkGraph.read(serialized_graph)
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
                NetworkGraph.of(hexStringToByteArray("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943").reversedArray())
        }
    } else {
        // first run, creating from scratch
        Global.router =
            NetworkGraph.of(hexStringToByteArray("000000000933ea01ad0ee984209779baaec3ced90fa3f408719526f8d77f4943").reversedArray())
    }

////    /*val route_handler = NetGraphMsgHandler.of(
////        router,
////        Option_AccessZ.none(),
////        logger
////    )*/

    // INITIALIZE THE CHANNELMANAGER ###############################################################
   // What it's used for: managing channel state


    val scorer = MultiThreadedLockableScore.of(Scorer.with_default().as_Score())

    // this is going to be the fee policy for __incoming__ channels. they are set upfront globally:
    val uc = UserConfig.with_default()
    val newChannelConfig = ChannelConfig.with_default()
    newChannelConfig.set_forwarding_fee_proportional_millionths(10000);
    newChannelConfig.set_forwarding_fee_base_msat(1000);

    val handshake = ChannelHandshakeConfig.with_default();
    handshake.set_minimum_depth(1);
    uc.set_own_channel_config(handshake);

    uc.set_channel_options(newChannelConfig);
    val newLim = ChannelHandshakeLimits.with_default()
    newLim.set_force_announced_channel_preference(false)
    uc.set_peer_channel_config_limits(newLim)

    try {
        if (serializedChannelManager != "") {
            // loading from disk
            Global.channelManagerConstructor = ChannelManagerConstructor(
                hexStringToByteArray(serializedChannelManager),
                channelMonitors,
                uc,
                Global.keysManager?.as_KeysInterface(),
                fee_estimator,
                Global.chainMonitor,
                Global.txFilter,
                Global.router!!.write(),
                tx_broadcaster,
                logger
            );
            Global.channelManager = Global.channelManagerConstructor!!.channel_manager;
            Global.channelManagerConstructor!!.chain_sync_completed(channel_manager_persister, scorer);
            Global.peerManager = Global.channelManagerConstructor!!.peer_manager;
            Global.nioPeerHandler = Global.channelManagerConstructor!!.nio_peer_handler;
            Global.router = Global.channelManagerConstructor!!.net_graph;
        } else {
            // fresh start
            Global.channelManagerConstructor = ChannelManagerConstructor(
                Network.LDKNetwork_Testnet,
                uc,
                hexStringToByteArray(latestBlockHash),
                latestBlockHeight,
                Global.keysManager?.as_KeysInterface(),
                fee_estimator,
                Global.chainMonitor,
                Global.router,
                tx_broadcaster,
                logger
            );
            Global.channelManager = Global.channelManagerConstructor!!.channel_manager;
            Global.channelManagerConstructor!!.chain_sync_completed(channel_manager_persister, scorer);
            Global.peerManager = Global.channelManagerConstructor!!.peer_manager;
            Global.nioPeerHandler = Global.channelManagerConstructor!!.nio_peer_handler;
            Global.router = Global.channelManagerConstructor!!.net_graph;
        }

        Global.nioPeerHandler!!.bind_listener(InetSocketAddress("0.0.0.0", 9735))
    } catch (e: Exception) {
        println("LDK: can't start, " + e.message);
    }
}
