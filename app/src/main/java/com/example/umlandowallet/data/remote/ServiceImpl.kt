package com.example.umlandowallet.data.remote

import android.R.attr
import android.R.attr.host
import com.example.umlandowallet.Global
import com.example.umlandowallet.toByteArray
import com.example.umlandowallet.toHex
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import org.ldk.structs.PeerManager
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketAddress


class ServiceImpl(private val client: HttpClient) : Service {
    override suspend fun getlatestBlockHash(): String {
//        return client.get("https://blockstream.info/testnet/api/blocks/tip/hash")
         return client.get("http://10.0.2.2:3002/blocks/tip/hash")
    }

    override suspend fun getlatestBlockHeight(): Int {
//        return client.get("https://blockstream.info/testnet/api/blocks/tip/height")
         return client.get("http://10.0.2.2:3002/blocks/tip/height")
    }

    override suspend fun broadcastTx(tx: ByteArray): String {
//        return client.post("https://blockstream.info/testnet/api/tx/${tx.toHex()}")
        return client.post("http://10.0.2.2:3002/tx/${tx.toHex()}")

    }

    override suspend fun getStatus(txid: String): HttpResponse {
//        return client.get("https://blockstream.info/testnet/api/tx/${txid}")
        return client.get("http://10.0.2.2:3002/tx/${txid}")

    }

    override suspend fun connectPeer(pubkeyHex: String, hostname: String, port: Int): Boolean {
        println("LDK: attempting to connect to peer $pubkeyHex")
        try {
            // Connect and wait for the handshake to complete.
            val address: SocketAddress = InetSocketAddress(hostname, port)
            Global.nioPeerHandler!!.connect(pubkeyHex.toByteArray(), address, 5555)

           println("LDK: successfully connected to peer $pubkeyHex")


            // The peer's pubkey will be present in the list of peer ids.
            val peerManager: PeerManager = Global.channelManagerConstructor!!.peer_manager
            val peerNodeIds = peerManager._peer_node_ids
        } catch (e: IOException) {
            // Handle failure to successfully connect to a peer.
            println("connectPeer exception: " + e.message)
            false
        }
//        return try {
//            Global.nioPeerHandler!!.connect(
//                pubkeyHex.toByteArray(),
//                InetSocketAddress(hostname, port), 5555
//            )
//            println("LDK: successfully connected to peer $pubkeyHex")
//            true
//        } catch (e: IOException) {
//            println("connectPeer exception: " + e.message)
//            false
//        }
    }
}