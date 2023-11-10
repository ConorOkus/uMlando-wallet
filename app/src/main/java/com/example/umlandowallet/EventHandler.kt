package com.example.umlandowallet

import android.util.Log
import com.example.umlandowallet.utils.LDKTAG
import com.example.umlandowallet.utils.write
import org.ldk.batteries.ChannelManagerConstructor
import org.ldk.structs.Event

// Responsible for backing up channel_manager bytes
object LDKEventHandler : ChannelManagerConstructor.EventHandler {
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