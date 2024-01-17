package com.example.umlandowallet

import android.util.Log
import com.example.umlandowallet.Global.channelManager
import com.example.umlandowallet.utils.LDKTAG
import com.example.umlandowallet.utils.storeEvent
import com.example.umlandowallet.utils.toHex
import org.bitcoindevkit.Address
import org.ldk.structs.*
import org.ldk.structs.PaymentPurpose.InvoicePayment
import org.ldk.util.UInt128
import kotlin.random.Random

@OptIn(ExperimentalUnsignedTypes::class)
fun handleEvent(event: Event) {
    if (event is Event.FundingGenerationReady) {
        Log.i(LDKTAG, "FundingGenerationReady")
        if (event.output_script.size == 34 && event.output_script[0].toInt() == 0 && event.output_script[1].toInt() == 32) {
            val rawTx =
                OnchainWallet.buildFundingTx(event.channel_value_satoshis, event.output_script)

            try {
                val fundingTx = channelManager!!.funding_transaction_generated(
                    event.temporary_channel_id,
                    event.counterparty_node_id,
                    rawTx.serialize().toUByteArray().toByteArray()
                )
                if (fundingTx is Result_NoneAPIErrorZ.Result_NoneAPIErrorZ_OK) {
                    Log.i(LDKTAG, "Funding transaction generated")
                }
                if (fundingTx is Result_NoneAPIErrorZ.Result_NoneAPIErrorZ_Err) {
                    Log.i(LDKTAG, "Error creating funding transaction: ${fundingTx.err}")
                }
            } catch (e: Exception) {
                Log.i(LDKTAG, "Error: ${e.message}")
            }

        }
    }

    if (event is Event.OpenChannelRequest) {
        Log.i(LDKTAG, "Event.OpenChannelRequest")

        val params = WritableMap()
        val userChannelId = UInt128(Random.nextLong(0, 100))

        params.putString("counterparty_node_id", event.counterparty_node_id.toHex())
        params.putString("temporary_channel_id", event.temporary_channel_id.toHex())
        params.putString("push_sat", (event.push_msat.toInt() / 1000).toString())
        params.putString("funding_satoshis", event.funding_satoshis.toString())
        params.putString("channel_type", event.channel_type.toString())

        Global.temporaryChannelId = event.temporary_channel_id
        Global.counterpartyNodeId = event.counterparty_node_id
        storeEvent("${Global.homeDir}/events_open_channel_request", params)
        Global.eventsFundingGenerationReady =
            Global.eventsFundingGenerationReady.plus(params.toString())

        val res = channelManager?.accept_inbound_channel(
            event.temporary_channel_id,
            event.counterparty_node_id,
            userChannelId
        )

        if (res != null) {
            if (res.is_ok) {
                Log.i(LDKTAG, "Open Channel Request Accepted")
            } else {
                Log.i(LDKTAG, "Open Channel Request Rejected")
            }
        }
    }

    if (event is Event.ChannelClosed) {
        Log.i(LDKTAG, "ChannelClosed")
        val params = WritableMap()
        val reason = event.reason
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
            params.putString("text", reason.peer_msg.toString())
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

    if (event is Event.ChannelPending) {
        Log.i(LDKTAG, "Event.ChannelPending")
        val params = WritableMap()
        params.putString("channel_id", event.channel_id.toHex())
        params.putString("tx_id", event.funding_txo._txid.toHex())
        params.putString("user_channel_id", event.user_channel_id.toString())
        storeEvent("${Global.homeDir}/events_channel_pending", params)
    }

    if (event is Event.ChannelReady) {
        Log.i(LDKTAG, "Event.ChannelReady")
        val params = WritableMap()
        params.putString("channel_id", event.channel_id.toHex())
        params.putString("user_channel_id", event.user_channel_id.toString())
        storeEvent("${Global.homeDir}/events_channel_ready", params)
    }

    if (event is Event.PaymentSent) {
        Log.i(LDKTAG, "Payment Sent")
    }

    if (event is Event.PaymentFailed) {
        Log.i(LDKTAG, "Payment Failed")
    }

    if (event is Event.PaymentPathFailed) {
        Log.i(LDKTAG, "Event.PaymentPathFailed${event.failure}")
    }

    if (event is Event.PendingHTLCsForwardable) {
        Log.i(LDKTAG, "Event.PendingHTLCsForwardable")
        channelManager?.process_pending_htlc_forwards()
    }

    if (event is Event.SpendableOutputs) {
        Log.i(LDKTAG, "Event.SpendableOutputs")
        val outputs = event.outputs
        try {
            val address = OnchainWallet.getNewAddress()
            val script = Address(address).scriptPubkey().toBytes().toUByteArray().toByteArray()
            val txOut: Array<TxOut> = arrayOf()
            val res = Global.keysManager?.inner?.spend_spendable_outputs(
                outputs,
                txOut,
                script,
                1000,
                null
            )

            if (res != null) {
                if (res.is_ok) {
                    val tx = (res as Result_TransactionNoneZ.Result_TransactionNoneZ_OK).res
                    val txs: Array<ByteArray> = arrayOf()
                    txs.plus(tx)

                    LDKBroadcaster.broadcast_transactions(txs)
                }
            }

        } catch (e: Exception) {
            Log.i(LDKTAG, "Error: ${e.message}")
        }

    }

    if (event is Event.PaymentClaimable) {
        Log.i(LDKTAG, "Event.PaymentClaimable")
        if (event.payment_hash != null) {
            val purpose = event.purpose as InvoicePayment
            val paymentPreimage = (purpose.payment_preimage as Option_ThirtyTwoBytesZ.Some).some

            channelManager?.claim_funds(paymentPreimage)
        }
    }

    if (event is Event.PaymentClaimed) {
        Log.i(LDKTAG, "Claimed Payment: ${event.payment_hash.toHex()}")
    }
}
