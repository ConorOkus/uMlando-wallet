package com.example.umlandowallet.data.remote

import android.util.Log
import com.example.umlandowallet.Global
import com.example.umlandowallet.data.MerkleProof
import com.example.umlandowallet.data.Tx
import com.example.umlandowallet.utils.toByteArray
import com.example.umlandowallet.utils.toHex
import com.example.umlandowallet.utils.LDKTAG
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.io.IOException
import java.net.InetSocketAddress


class ServiceImpl(private val client: HttpClient) : Service {
    var baseUrl: String = "https://blockstream.info/testnet/api/"
    override suspend fun getLatestBlockHash(): String {
        val httpResponse: HttpResponse = client.get("${baseUrl}blocks/tip/hash")
        return httpResponse.body()
    }

    override suspend fun getLatestBlockHeight(): Int {
        val httpResponse: HttpResponse = client.get("${baseUrl}blocks/tip/height")
        return httpResponse.body<Int>().toInt()
    }

    override suspend fun broadcastTx(tx: ByteArray): String {
        val response: HttpResponse = client.post("${baseUrl}tx") {
            setBody(tx.toHex())
        }

        return response.body()
    }

    override suspend fun getTx(txid: String): Tx {
        return client.get("${baseUrl}${txid}").body()
    }

    override suspend fun getTxHex(txid: String): String {
        return client.get("${baseUrl}${txid}/hex").body()
    }

    override suspend fun getHeader(hash: String): String {
        return client.get("${baseUrl}block/${hash}/header").body()
    }

    override suspend fun getMerkleProof(txid: String): MerkleProof {
        return client.get("${baseUrl}${txid}/merkle-proof").body()
    }

    override suspend fun connectPeer(pubkeyHex: String, hostname: String, port: Int) {
        Log.i(LDKTAG, "LDK: attempting to connect to peer $pubkeyHex")
        try {
            Global.channelManagerConstructor!!.nio_peer_handler!!.connect(
                pubkeyHex.toByteArray(),
                InetSocketAddress(hostname, port), 5555
            )
            Log.i(LDKTAG, "LDK: successfully connected to peer $pubkeyHex")
        } catch (e: IOException) {
            Log.i(LDKTAG, "connectPeer exception: " + e.message)
        }
    }
}