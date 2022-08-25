package com.example.umlandowallet.data.remote

import com.example.umlandowallet.Global
import com.example.umlandowallet.toByteArray
import com.example.umlandowallet.toHex
import io.ktor.client.*
import io.ktor.client.request.*
import java.io.IOException
import java.net.InetSocketAddress

class ServiceImpl(private val client: HttpClient) : Service {
    override suspend fun getlatestBlockHash(): String {
        return client.get("https://blockstream.info/testnet/api/blocks/tip/hash")
        // return client.get("http://10.0.2.2:3002/blocks/tip/hash")
    }

    override suspend fun getlatestBlockHeight(): Int {
        return client.get("https://blockstream.info/testnet/api/blocks/tip/height")
        // return client.get("http://10.0.2.2:3002/blocks/tip/height")
    }

    override suspend fun broadcastTx(tx: ByteArray): String {
        return client.post("https://blockstream.info/testnet/api/tx/${tx.toHex()}")
    }

    override suspend fun connectPeer(pubkeyHex: String, hostname: String, port: Int): Boolean {
        println("LDK: attempting to connect to peer $pubkeyHex")
        return try {
            Global.nioPeerHandler!!.connect(
                pubkeyHex.toByteArray(),
                InetSocketAddress(hostname, port), 5555
            )
            println("LDK: successfully connected to peer $pubkeyHex")
            true
        } catch (e: IOException) {
            println("connectPeer exception: " + e.message)
            false
        }
    }
}