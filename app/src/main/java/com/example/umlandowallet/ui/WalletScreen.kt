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
import com.example.umlandowallet.utils.LDKTAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun WalletScreen() {
    val accessImpl = AccessImpl()

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
                CoroutineScope(Dispatchers.IO).launch {
                    accessImpl.sync()
                }

                Log.i(LDKTAG, "Wallet synced")
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
