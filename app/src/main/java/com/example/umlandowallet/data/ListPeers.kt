package com.example.umlandowallet.data

import com.example.umlandowallet.Global
import com.example.umlandowallet.byteArrayToHex

fun listPeers(): Array<ByteArray> {
//    if (Global.peerManager === null) {
//        return "No peer manager initiated"
//    }

    return Global.peerManager!!.get_peer_node_ids()
}