package com.example.umlandowallet

import com.example.umlandowallet.data.OnchainWallet
import org.ldk.structs.*
import kotlin.random.Random

fun handleEvent(event: Event) {
    if (event is Event.FundingGenerationReady) {
        println("FundingGenerationReady")
        val funding_spk = event.output_script
        if (funding_spk.size == 34 && funding_spk[0].toInt() == 0 && funding_spk[1].toInt() == 32) {
            val params = WritableMap()

            params.putString("counterparty_node_id", event.counterparty_node_id.toHex())
            params.putString("channel_value_satoshis", event.channel_value_satoshis.toString())
            params.putString("output_script", event.output_script.toHex())
            params.putString("temporary_channel_id", event.temporary_channel_id.toHex())
            params.putString("user_channel_id", event.user_channel_id.toString())
            Global.temporaryChannelId = event.temporary_channel_id
            Global.counterpartyNodeId = event.counterparty_node_id
            storeEvent("${Global.homeDir}/events_funding_generation_ready", params)
            Global.eventsFundingGenerationReady = Global.eventsFundingGenerationReady.plus(params.toString())

            val rawTx = OnchainWallet.buildFundingTx(event.channel_value_satoshis, event.output_script)

            Global.channelManager!!.funding_transaction_generated(
                event.temporary_channel_id,
                event.counterparty_node_id,
                rawTx
            )
        }
    }

    if (event is Event.OpenChannelRequest) {
        val params = WritableMap()
        val userChannelId = Random.nextLong(0, 100)

        params.putString("counterparty_node_id", event.counterparty_node_id.toHex())
        params.putString("temporary_channel_id", event.temporary_channel_id.toHex())
        params.putString("push_sat", (event.push_msat.toInt() / 1000).toString())
        params.putString("funding_satoshis", event.funding_satoshis.toString())
        params.putString("channel_type", event.channel_type.toString())

        Global.temporaryChannelId = event.temporary_channel_id
        Global.counterpartyNodeId = event.counterparty_node_id
        storeEvent("${Global.homeDir}/events_open_channel_request", params)
        Global.eventsFundingGenerationReady = Global.eventsFundingGenerationReady.plus(params.toString())

        Global.channelManager!!.accept_inbound_channel(
            event.temporary_channel_id,
            event.counterparty_node_id,
            userChannelId
        )
    }

    if (event is Event.ChannelClosed) {
        println("ChannelClosed");
        val params = WritableMap()
        val reason = event.reason;
        params.putString("channel_id", event.channel_id.toHex())
        params.putString("user_channel_id", event.user_channel_id.toString())

        if (reason is ClosureReason.CommitmentTxConfirmed) {
            params.putString("reason", "CommitmentTxConfirmed")
        }
        if (reason is ClosureReason.CooperativeClosure) {
            params.putString("reason", "CooperativeClosure")
        }
        if (reason is ClosureReason.CounterpartyForceClosed) {
            params.putString("reason", "CounterpartyForceClosed")
            params.putString("text", reason.peer_msg)
        }
        if (reason is ClosureReason.DisconnectedPeer) {
            params.putString("reason", "DisconnectedPeer")
        }
        if (reason is ClosureReason.HolderForceClosed) {
            params.putString("reason", "HolderForceClosed")
        }
        if (reason is ClosureReason.OutdatedChannelManager) {
            params.putString("reason", "OutdatedChannelManager")
        }
        if (reason is ClosureReason.ProcessingError) {
            params.putString("reason", "ProcessingError")
            params.putString("text", reason.err)
        }
        storeEvent("${Global.homeDir}/events_channel_closed", params)
        Global.eventsChannelClosed = Global.eventsChannelClosed.plus(params.toString())
    }
}
