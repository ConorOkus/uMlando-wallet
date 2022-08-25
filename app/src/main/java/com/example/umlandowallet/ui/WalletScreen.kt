package com.example.umlandowallet.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umlandowallet.Global
import com.example.umlandowallet.createBlockchain
import com.example.umlandowallet.data.remote.Access
import com.example.umlandowallet.toHex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bitcoindevkit.PartiallySignedBitcoinTransaction
import org.bitcoindevkit.Progress
import org.bitcoindevkit.Script
import org.bitcoindevkit.TxBuilder

private const val TAG = "WalletScreen"

@Composable
fun WalletScreen() {
    Column(
        modifier = Modifier
            .padding(top = 48.dp)
            .fillMaxSize()
    ) {
        val balance = remember { mutableStateOf(0) }

        // Title
        Text(
            text = "Wallet",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )

        Text(
            text = "Lightning Balance: ${balance.value} sat",
            fontSize = 18.sp,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
        )
        Button(
            onClick = {
                val relevantTxIdsFromChannelManager: Array<ByteArray> = Global.channelManager!!.as_Confirm()._relevant_txids
                val relevantTxIdsFromChainMonitor: Array<ByteArray> = Global.chainMonitor!!.as_Confirm()._relevant_txids

                val relevantTxIds: Array<ByteArray> = relevantTxIdsFromChannelManager + relevantTxIdsFromChainMonitor

                CoroutineScope(Dispatchers.IO).launch {
                    val access = Access.create()
                    // Sync BDK wallet
                    access.syncWallet(Global.wallet!!, LogProgress)

                    // Sync LDK/Lightning
                    access.syncTransactionsUnconfirmed(relevantTxIds, Global.channelManager!!, Global.chainMonitor!!)
                    access.syncTransactionConfirmed(relevantTxIds, Global.channelManager!!, Global.chainMonitor!!)
                }

                Log.i(TAG, "Wallet synced")
            },
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "Sync",
            )
        }
    }
}

fun buildFundingTx(value: Long, script: ByteArray): ByteArray {
    val blockchain = createBlockchain()
    Global.wallet!!.sync(blockchain, LogProgress)
    val scriptListUByte: List<UByte> = script.toUByteArray().asList()
    val outputScript: Script = Script(scriptListUByte)
    val (psbt, txDetails) = TxBuilder()
        .addRecipient(outputScript, value.toULong())
        .feeRate(4.0F)
        .finish(Global.wallet!!)
    Global.wallet!!.sign(psbt)
    val rawTx = psbt.extractTx().toUByteArray().toByteArray()
    println("The raw funding tx is ${rawTx.toHex()}")
    return rawTx
}

fun sign(psbt: PartiallySignedBitcoinTransaction) {
    Global.wallet!!.sign(psbt)
}

fun broadcast(signedPsbt: PartiallySignedBitcoinTransaction): String {
    val blockchain = createBlockchain()
    blockchain.broadcast(signedPsbt)
    return signedPsbt.txid()
}

object LogProgress: Progress {
    override fun update(progress: Float, message: String?) {
        Log.d(TAG, "updating wallet $progress $message")
    }
}
