package com.example.umlandowallet.utils

import android.util.Log
import com.example.umlandowallet.Global
import com.example.umlandowallet.WritableMap
import java.io.File
import com.google.common.io.BaseEncoding

fun ByteArray.toHex(): String {
    return BaseEncoding.base16().encode(this).lowercase()
}

fun String.toByteArray(): ByteArray {
    return BaseEncoding.base16().decode(this.uppercase())
}

fun storeEvent(eventsPath: String, params: WritableMap) {
    val directory = File(eventsPath)
    if (!directory.exists()) {
        directory.mkdir()
    }

    File(eventsPath + "/" + System.currentTimeMillis() + ".json").writeText(params.toString())
}

fun write(identifier: String, data: ByteArray?) {
    val fileName = "${Global.homeDir}/$identifier"
    val file = File(fileName)
    if(data != null) {
        Log.i(LDKTAG, "Writing to file: $fileName")
        file.writeBytes(data)
    }
}