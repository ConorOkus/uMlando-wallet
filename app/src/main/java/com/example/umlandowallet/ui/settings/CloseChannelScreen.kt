package com.example.umlandowallet.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umlandowallet.Global.channelManager
import com.example.umlandowallet.utils.LDKTAG
import com.example.umlandowallet.utils.convertToByteArray
import com.example.umlandowallet.utils.toByteArray
import org.ldk.structs.Result_NoneAPIErrorZ

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseChannelScreen() {
    var pubKey by remember {
        mutableStateOf("")
    }
    var channelId by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    )
    {
        Text(
            text = "Close a channel",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )
        TextField(
            value = pubKey,
            onValueChange = { pubKey = it },
            placeholder = {
                Text("Peer public node ID")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        TextField(
            value = channelId,
            onValueChange = { channelId = it },
            placeholder = {
                Text("Channel ID")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Button(
            onClick = {
                closeChannel(channelId, pubKey)
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(text = "Connect")
        }
    }
}

private fun closeChannel(channelId: String, pubKey: String) {
    val res = channelManager!!.close_channel(hexStringToByteArray(channelId), hexStringToByteArray(pubKey))

    if (res is Result_NoneAPIErrorZ.Result_NoneAPIErrorZ_Err) {
        Log.i(LDKTAG, "ERROR: failed to close channel with: $pubKey")
    }

    if(res.is_ok) {
        Log.i(LDKTAG, "EVENT: initiated channel close with peer: $pubKey")
    }
}

fun hexStringToByteArray(hexString: String): ByteArray {
    val length = hexString.length
    if (length % 2 != 0) {
        return byteArrayOf() // Return an empty byte array if the string length is odd
    }
    val bytes = ByteArray(length / 2)
    for (i in 0 until length step 2) {
        val nextIndex = i + 2
        try {
            val b = hexString.substring(i, nextIndex).toInt(16).toByte()
            bytes[i / 2] = b
        } catch (e: NumberFormatException) {
            return byteArrayOf() // Return an empty byte array if parsing fails
        }
    }
    return bytes
}
