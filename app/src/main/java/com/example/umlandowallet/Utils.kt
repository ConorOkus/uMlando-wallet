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

fun hexStringToByteArray(strArg: String): ByteArray {
    val HEX_CHARS = "0123456789ABCDEF"
    val str = strArg.uppercase();

    if (str.length % 2 != 0) return hexStringToByteArray("");

    val result = ByteArray(str.length / 2)

    for (i in 0 until str.length step 2) {
        val firstIndex = HEX_CHARS.indexOf(str[i]);
        val secondIndex = HEX_CHARS.indexOf(str[i + 1]);

        val octet = firstIndex.shl(4).or(secondIndex)
        result.set(i.shr(1), octet.toByte())
    }

    return result
}

fun storeEvent(eventsPath: String, params: WritableMap) {
    val directory = File(eventsPath)
    if (!directory.exists()) {
        directory.mkdir()
    }

    File(eventsPath + "/" + System.currentTimeMillis() + ".json").writeText(params.toString())
}