package com.example.umlandowallet

import android.util.Log
import com.example.umlandowallet.utils.LDKTAG
import com.example.umlandowallet.utils.toHex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bitcoindevkit.Transaction
import org.ldk.structs.BroadcasterInterface

// To create a transaction broadcaster we need provide an object that implements the BroadcasterInterface
// which has 1 function broadcast_transaction(tx: ByteArray?)
object LDKBroadcaster : BroadcasterInterface.BroadcasterInterfaceInterface {
    @OptIn(ExperimentalUnsignedTypes::class)
    override fun broadcast_transactions(txs: Array<out ByteArray>??) {
        txs?.let { transactions ->
            CoroutineScope(Dispatchers.IO).launch {
                transactions.forEach { txByteArray ->
                    val uByteArray = txByteArray.toUByteArray()
                    val transaction = Transaction(uByteArray.toList())

                    OnchainWallet.broadcastRawTx(transaction)

                    Log.i(LDKTAG, "The raw transaction broadcast is: ${txByteArray.toHex()}")
                }
            }
        } ?: throw(IllegalStateException("Broadcaster attempted to broadcast a null transaction"))
    }

}