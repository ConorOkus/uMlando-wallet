package com.example.umlandowallet

import org.ldk.structs.*

fun handleEvent(event: Event) {
    if (event is Event.ChannelClosed) {
        println("ChannelClosed");
        val params = WritableMap()
        val reason = event.reason;
        params.putString("channel_id", byteArrayToHex(event.channel_id));
        params.putString("user_channel_id", event.user_channel_id.toString());

        if (reason is ClosureReason.CommitmentTxConfirmed) {
            params.putString("reason", "CommitmentTxConfirmed");
        }
        if (reason is ClosureReason.CooperativeClosure) {
            params.putString("reason", "CooperativeClosure");
        }
        if (reason is ClosureReason.CounterpartyForceClosed) {
            params.putString("reason", "CounterpartyForceClosed");
            params.putString("text", reason.peer_msg);
        }
        if (reason is ClosureReason.DisconnectedPeer) {
            params.putString("reason", "DisconnectedPeer");
        }
        if (reason is ClosureReason.HolderForceClosed) {
            params.putString("reason", "HolderForceClosed");
        }
        if (reason is ClosureReason.OutdatedChannelManager) {
            params.putString("reason", "OutdatedChannelManager");
        }
        if (reason is ClosureReason.ProcessingError) {
            params.putString("reason", "ProcessingError");
            params.putString("text", reason.err);
        }
        storeEvent("${Global.homeDir}/events_channel_closed", params)
        Global.eventsChannelClosed = Global.eventsChannelClosed.plus(params.toString())
    }
}