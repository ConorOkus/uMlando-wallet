package com.example.umlandowallet.data

import com.example.umlandowallet.Global
import com.example.umlandowallet.byteArrayToHex

fun listPeers(): String {
    if (Global.peerManager === null) {
        return "No peer manager initiated"
    }
    val peerNodeIds: Array<ByteArray> = Global.peerManager!!.get_peer_node_ids()
    var json = "["
    var first = true
    peerNodeIds.iterator().forEach {
        if (!first) json += ","
        first = false
        json += "\"" + byteArrayToHex(it) + "\""
    }
    json += "]"
    return json
}