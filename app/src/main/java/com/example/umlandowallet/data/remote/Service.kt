package com.example.umlandowallet.data.remote

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*

interface Service {

    suspend fun getlatestBlockHash() : String

    suspend fun getlatestBlockHeight() : Int

    suspend fun connectPeer(pubkeyHex: String, hostname: String, port: Int) : Boolean

    companion object {
        fun create() : Service {
            return ServiceImpl(
                client = HttpClient(Android) {
                    install(Logging) {
                        level = LogLevel.ALL
                    }
                    install(JsonFeature) {
                        serializer = KotlinxSerializer()
                    }
                }
            )
        }
    }
}