package com.example.umlandowallet.data.remote

import com.example.umlandowallet.Global
import com.example.umlandowallet.data.Tx
import com.example.umlandowallet.toByteArray
import com.example.umlandowallet.toHex
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.io.IOException
import java.net.InetSocketAddress


class ServiceImpl(private val client: HttpClient) : Service {
    override suspend fun getlatestBlockHash(): String {
        val httpResponse: HttpResponse = client.get("http://10.0.2.2:3002/blocks/tip/hash")
        return httpResponse.body()
    }

    override suspend fun getlatestBlockHeight(): Int {
        val httpResponse: HttpResponse = client.get("http://10.0.2.2:3002/blocks/tip/height")
        return httpResponse.body<Int>().toInt()
    }

    override suspend fun broadcastTx(tx: ByteArray): String {
        val response: HttpResponse = client.post("http://10.0.2.2:3002/tx") {
            setBody(tx.toHex())
        }

        return response.body()
    }

    override suspend fun getStatus(txid: String): Tx {
        val tx: Tx = client.get("http://10.0.2.2:3002/tx/${txid}").body()

        return tx
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