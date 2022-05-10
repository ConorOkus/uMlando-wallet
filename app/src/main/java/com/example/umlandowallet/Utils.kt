package com.example.umlandowallet

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

fun sha256(input:String): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

fun byteArrayToHex(bytesArg: ByteArray): String {
    return bytesArg.joinToString("") { String.format("%02X", (it.toInt() and 0xFF)) }.lowercase()
}

fun String.hexStringToByteArray(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun storeEvent(eventsPath: String, params: WritableMap) {
    val directory = File(eventsPath)
    if (!directory.exists()) {
        directory.mkdir()
    }

    File(eventsPath + "/" + System.currentTimeMillis() + ".json").writeText(params.toString())
}