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

        var serializedChannelManager: ByteArray? = null
        var serializedChannelMonitors = arrayOf<ByteArray>()

        val channelManagerFile = File("${Global.homeDir}/channel-manager.bin")
        if(channelManagerFile.exists()) {
            serializedChannelManager = channelManagerFile.absoluteFile.readBytes()
        }

        // Read Channel Monitor state from disk
        // Initialize the hashmap where we'll store the `ChannelMonitor`s read from disk.
        // This hashmap will later be given to the `ChannelManager` on initialization.
        val channelMonitorDirectory = File("${Global.homeDir}/channels/")
        if (channelMonitorDirectory.isDirectory) {
            val files: Array<String> = channelMonitorDirectory.list()
            if (files.isNotEmpty()) {
                val channelMonitorList = serializedChannelMonitors.toMutableList()
                files.forEach {
                    channelMonitorList.add(File("${channelMonitorDirectory}/${it}").readBytes())
                }

                serializedChannelMonitors = channelMonitorList.toTypedArray()

            }
        } else {
            channelMonitorDirectory.mkdir()
            Log.i(LDKTAG, "Creating directory at $channelMonitorDirectory")
        }

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
