package com.example.umlandowallet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.umlandowallet.data.OnchainWallet
import com.example.umlandowallet.utils.LDKTAG
import java.io.File

class DispatchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Global.homeDir = filesDir.absolutePath + "/uMlando"
        val directory = File(Global.homeDir)
        if(!directory.exists()) {
            directory.mkdir()
        }

        // Initialize the LDK data directory if necessary.
        Global.homeDir += "/" + "ldk-data"
        val ldkDataDirectory = File(Global.homeDir)
        if(!ldkDataDirectory.exists()) {
            ldkDataDirectory.mkdir()
            Log.i(LDKTAG, "Creating directory at $ldkDataDirectory")
        }

        val latestBlockHeight = OnchainWallet.getHeight()
        val latestBlockHash = OnchainWallet.getBlockHash(latestBlockHeight)

        var serializedChannelManager = ""
        var serializedChannelMonitors = ""
        var monitors = arrayOf<String>()

        File(Global.homeDir).walk().forEach {
            if(it.name.startsWith(Global.prefixChannelManager)) {
                serializedChannelManager = it.absoluteFile.readText(Charsets.UTF_8)
            }
            if(it.name.startsWith(Global.prefixChannelMonitor)) {
                val serializedMonitor = it.absoluteFile.readText(Charsets.UTF_8)
                monitors = monitors.plus(serializedMonitor)
            }
        }

        serializedChannelMonitors = monitors.joinToString(separator = ",")

        Log.i(LDKTAG, "Successfully created/restored wallet with mnemonic ${OnchainWallet.recoveryPhrase()}")

        start(
            OnchainWallet.getLdkEntropy(),
            latestBlockHeight.toInt(),
            latestBlockHash,
            serializedChannelManager,
            serializedChannelMonitors
        )

        startActivity(Intent(this, MainActivity::class.java))
    }
}
