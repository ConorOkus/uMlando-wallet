package com.example.umlandowallet

import android.util.Log
import com.example.umlandowallet.utils.LDKTAG
import org.ldk.structs.Logger
import org.ldk.structs.Record

// To create a Logger we need to provide an object that implements the LoggerInterface
// which has 1 function: log(record: Record?): Unit
object LDKLogger : Logger.LoggerInterface {
    override fun log(record: Record?) {
        val rawLog = record!!._args.toString()
        Log.i(LDKTAG, rawLog)
    }
}