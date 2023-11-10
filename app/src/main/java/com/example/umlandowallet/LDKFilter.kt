package com.example.umlandowallet

import android.util.Log
import com.example.umlandowallet.data.WatchedTransaction
import com.example.umlandowallet.utils.LDKTAG
import com.example.umlandowallet.utils.storeEvent
import com.example.umlandowallet.utils.toHex
import io.ktor.util.*
import org.ldk.structs.*

// Filter allows LDK to let you know what transactions you should filter blocks for. This is
// useful if you pre-filter blocks or use compact filters. Otherwise, LDK will need full blocks.
object LDKTxFilter : Filter.FilterInterface {
    var txids: Array<ByteArray> = arrayOf()
    var outputs: Array<WatchedOutput> = arrayOf()

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

        txids.plus(txid)

        Log.i(LDKTAG, Global.relevantTxs.toString())
    }

    override fun register_output(output: WatchedOutput) {
        Log.i(LDKTAG, "register_output")

        val index = output._outpoint._index.toString()
        val scriptPubkey = output._script_pubkey.toHex()

        val params = WritableMap()

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

        outputs.plus(output)

        Log.i(LDKTAG, Global.relevantOutputs.toString())
    }
}