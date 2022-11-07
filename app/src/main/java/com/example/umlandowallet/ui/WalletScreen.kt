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
import com.example.umlandowallet.ChannelManagerEventHandler
import com.example.umlandowallet.Global
import com.example.umlandowallet.data.OnchainWallet
import com.example.umlandowallet.data.remote.Access
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                    access.syncWallet(OnchainWallet)

                    // Sync LDK/Lightning
                    access.syncTransactionsUnconfirmed(relevantTxIds, Global.channelManager!!, Global.chainMonitor!!)
                    access.syncTransactionConfirmed(relevantTxIds, Global.channelManager!!, Global.chainMonitor!!)
                    access.syncBestBlockConnected(Global.channelManager!!, Global.chainMonitor!!)

                    Global.channelManagerConstructor!!.chain_sync_completed(
                        ChannelManagerEventHandler, Global.scorer!!)
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
