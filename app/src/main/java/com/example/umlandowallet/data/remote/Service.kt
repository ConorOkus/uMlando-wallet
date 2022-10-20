package com.example.umlandowallet.data.remote

import com.example.umlandowallet.data.Tx
import com.example.umlandowallet.data.TxStatus
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


interface Service {

    suspend fun getlatestBlockHash() : String

    suspend fun getlatestBlockHeight() : Int

    suspend fun broadcastTx(tx: ByteArray) : String

    suspend fun getStatus(txid: String) : Tx

    suspend fun connectPeer(pubkeyHex: String, hostname: String, port: Int) : Boolean

    companion object {
        fun create() : Service {
            return ServiceImpl(
                client = HttpClient() {
                    install(Logging) {
                        logger = Logger.DEFAULT
                        level = LogLevel.HEADERS
                    }
                    install(ContentNegotiation) {
                        json(
                            json = Json {
                                prettyPrint = true
                                isLenient = true
                                ignoreUnknownKeys = true
                            }
                        )
                    }
                }
            )
        }
    }
}