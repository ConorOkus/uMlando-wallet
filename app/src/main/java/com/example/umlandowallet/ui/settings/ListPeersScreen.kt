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
import com.example.umlandowallet.toHex

@Composable
fun ListPeersScreen() {
    Column(
        modifier = Modifier
            .padding(top = 48.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Currently Connected Peers",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )

        val peers = Global.peerManager!!.get_peer_node_ids()
        val peersList: MutableList<String> = mutableListOf()
        peers.forEach {
            peersList.add(it.toHex())
        }
        peersList.forEach { peer ->
            Text(
                text = peer,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp)
            )
        }
    }
}



// fun ListPeers() {
//     val _peerList = remember { MutableStateFlow(listOf<String>()) }
//     val peerList by remember { _peerList }.collectAsState()
//     Button(
//         onClick = {
//             val peers = Global.peerManager!!.get_peer_node_ids()
//             val newList = ArrayList(peerList)
//
//             println("List peers: $peers")
//
//             peers.iterator().forEach {
//                 newList.add(it.toHex())
//             }
//
//             _peerList.value = newList
//         },
//     ) {
//         Text(text = "List Peers")
//     }
//     Spacer(modifier = Modifier.height(8.dp))
//     LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
//         if(peerList.isNotEmpty()) {
//             items(peerList) { peer ->
//                 Text(text = peer)
//                 Spacer(modifier = Modifier.height(8.dp))
//                 Divider()
//                 Spacer(modifier = Modifier.height(8.dp))
//             }
//         }
//     }
// }

