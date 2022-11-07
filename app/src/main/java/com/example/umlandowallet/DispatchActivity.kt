package com.example.umlandowallet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.bitcoindevkit.*
import java.io.File

private const val TAG = "DispatchActivity"

class DispatchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val blockchain = createBlockchain()
        Global.blockchain = blockchain

        var latestBlockHeight = blockchain.getHeight()
        var latestBlockHash = blockchain.getBlockHash(latestBlockHeight)

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
        }

        var serializedChannelManager = ""
        var serializedChannelMonitors = ""
        var monitors = arrayOf<String>()

        File(Global.homeDir).walk().forEach {
            if(it.name.startsWith(Global.prefixChannelManager)) {
                serializedChannelManager = it.absoluteFile.readText(Charsets.UTF_8)
            }
            if(it.name.startsWith(Global.prefixChannelMonitor)) {
                val serializedMonitor = it.absoluteFile.readText(Charsets.UTF_8);
                monitors = monitors.plus(serializedMonitor)
            }
        }

        serializedChannelMonitors = monitors.joinToString(separator = ",")

        var mnemonic: String

        try {
            mnemonic = File(Global.homeDir + "/" + "mnemonic").readText()
        } catch (e: Throwable) {
            // if mnemonic doesn't exist, generate one and save it
            Log.i(TAG, "No mnemonic backup, we'll create a new wallet")
            mnemonic = generateMnemonic(WordCount.WORDS12)
            File(Global.homeDir + "/" + "mnemonic").writeText(mnemonic)
        }
        
        val descriptorSecretKey = DescriptorSecretKey(Network.REGTEST, mnemonic, null)

        val derivedKey = descriptorSecretKey.derive(DerivationPath("m/84h/1h/0h"))
        val externalDescriptor = "wpkh(${derivedKey.extend(DerivationPath("m/0")).asString()})"
        val internalDescriptor = "wpkh(${derivedKey.extend(DerivationPath("m/1")).asString()})"

        val ldkEntropy = getEntropy(mnemonic)

        val databaseConfig = DatabaseConfig.Memory

        Global.wallet = Wallet(
            internalDescriptor,
            externalDescriptor,
            Network.REGTEST,
            databaseConfig,
        )

        Log.i(TAG, "Successfully created/restored wallet with mnemonic $mnemonic")

        start(ldkEntropy.toHex(), latestBlockHeight.toInt(), latestBlockHash, serializedChannelManager, serializedChannelMonitors)

        startActivity(Intent(this, MainActivity::class.java))
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
private fun getEntropy(mnemonic: String): ByteArray {
    val bip32RootKey: DescriptorSecretKey = DescriptorSecretKey(
        network = Network.REGTEST,
        mnemonic = mnemonic,
        password = null,
    )
    val derivationPath = DerivationPath("m/535h")
    val child: DescriptorSecretKey = bip32RootKey.derive(derivationPath)
    val entropy: ByteArray = child.secretBytes().toUByteArray().toByteArray()

    println("Entropy used for LDK is ${entropy.toHex()}")
    return entropy
}

fun createBlockchain(): Blockchain {
    val esploraURL: String = "http://10.0.2.2:3002"

    val blockchainConfig = BlockchainConfig.Esplora(EsploraConfig(esploraURL, null, 5u, 20u, null))

    return Blockchain(blockchainConfig)
}