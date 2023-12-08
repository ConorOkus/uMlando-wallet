package com.example.umlandowallet.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umlandowallet.Global
import com.example.umlandowallet.utils.toHex

@Composable
fun NodeIdScreen() {
    Column(
        modifier = Modifier
            .padding(top = 48.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Node ID",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )

        val nodeIdByteArray = Global.channelManager?._our_node_id
        println(nodeIdByteArray!!.toHex())
        Text(
            text = nodeIdByteArray.toHex(),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp)
        )
    }
}
