package com.example.umlandowallet.data.remote

import android.util.Log
import com.example.umlandowallet.Global
import com.example.umlandowallet.data.MerkleProof
import com.example.umlandowallet.data.Tx
import com.example.umlandowallet.data.TxStatus
import com.example.umlandowallet.toByteArray
import com.example.umlandowallet.toHex
import com.example.umlandowallet.utils.LDKTAG
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.io.IOException
import java.net.InetSocketAddress


class ServiceImpl(private val client: HttpClient) : Service {
    override suspend fun getLatestBlockHash(): String {
        val httpResponse: HttpResponse = client.get("http://10.0.2.2:3002/blocks/tip/hash")
        return httpResponse.body()
    }

    override suspend fun getLatestBlockHeight(): Int {
        val httpResponse: HttpResponse = client.get("http://10.0.2.2:3002/blocks/tip/height")
        return httpResponse.body<Int>().toInt()
    }

    override suspend fun broadcastTx(tx: ByteArray): String {
        val response: HttpResponse = client.post("http://10.0.2.2:3002/tx") {
            setBody(tx.toHex())
        }

        return response.body()
    }

    override suspend fun getTx(txid: String): Tx {
        return client.get("http://10.0.2.2:3002/tx/${txid}").body()
    }

    override suspend fun getTxHex(txid: String): String {
        return client.get("http://10.0.2.2:3002/tx/${txid}/hex").body()
    }

    override suspend fun getHeader(hash: String): String {
        return client.get("http://10.0.2.2:3002/block/${hash}/header").body()
    }

    override suspend fun getMerkleProof(txid: String): MerkleProof {
        return client.get("http://10.0.2.2:3002/tx/${txid}/merkle-proof").body()
    }

    override suspend fun connectPeer(pubkeyHex: String, hostname: String, port: Int) {
        Log.i(LDKTAG, "LDK: attempting to connect to peer $pubkeyHex")
        try {
            Global.nioPeerHandler!!.connect(
                pubkeyHex.toByteArray(),
                InetSocketAddress(hostname, port), 5555
            )
            Log.i(LDKTAG, "LDK: successfully connected to peer $pubkeyHex")
        } catch (e: IOException) {
            Log.i(LDKTAG, "connectPeer exception: " + e.message)
        }
    }
}