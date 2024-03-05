package com.example.umlandowallet.data.remote

import com.example.umlandowallet.OnchainWallet

interface Access {
    suspend fun sync()

    suspend fun syncWallet(onchainWallet: OnchainWallet)
}