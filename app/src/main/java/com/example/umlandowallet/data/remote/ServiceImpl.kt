package com.example.umlandowallet.data.remote

import com.example.umlandowallet.Global
import com.example.umlandowallet.hexStringToByteArray
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.io.IOException
import java.net.InetSocketAddress

class ServiceImpl(private val client: HttpClient) : Service {
    override suspend fun getlatestBlockHash(): String {
//        return client.get("http://10.0.2.2:3002/blocks/tip/hash")
        return client.get("https://blockstream.info/api/blocks/tip/hash")
    }

    override suspend fun getlatestBlockHeight(): Int {
//        return client.get("http://10.0.2.2:3002/blocks/tip/height")
        return client.get("https://blockstream.info/api/blocks/tip/height")
    }

    override suspend fun getLatestRapidSnapshot(lastRapidSyncTimestamp: String): ByteArray {
        println("lastRapidSyncTimestamp: $lastRapidSyncTimestamp")
        val httpResponse: HttpResponse = client.get("https://arik.ngrok.io/snapshot/$lastRapidSyncTimestamp")

        if (httpResponse.status.value in 200..299) {
            println("Successfully fetched latest snapshot!")
        }

        return httpResponse.readBytes()
    }

    override suspend fun connectPeer(pubkeyHex: String, hostname: String, port: Int): Boolean {
        println("LDK: attempting to connect to peer $pubkeyHex")
        return try {
            Global.nioPeerHandler!!.connect(
                pubkeyHex.hexStringToByteArray(),
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