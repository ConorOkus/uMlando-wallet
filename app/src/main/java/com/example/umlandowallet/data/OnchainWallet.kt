package com.example.umlandowallet.data

import android.util.Log
import com.example.umlandowallet.Global
import com.example.umlandowallet.toHex
import com.example.umlandowallet.utils.LDKTAG
import org.bitcoindevkit.*
import java.io.File

// The onchain wallet is currently always in regtest mode
object OnchainWallet {
    private lateinit var onchainWallet: Wallet
    private val blockchain: Blockchain = createBlockchain()

    init {
        createOnchainWallet()
    }

    private fun createOnchainWallet() {
        val mnemonic = loadMnemonic()
        val descriptorSecretKey = DescriptorSecretKey(Network.REGTEST, mnemonic, null)
        val derivedKey = descriptorSecretKey.derive(DerivationPath("m/84h/1h/0h"))
        val externalDescriptor = "wpkh(${derivedKey.extend(DerivationPath("m/0")).asString()})"
        val internalDescriptor = "wpkh(${derivedKey.extend(DerivationPath("m/1")).asString()})"
        val databaseConfig = DatabaseConfig.Memory

        onchainWallet = Wallet(
            internalDescriptor,
            externalDescriptor,
            Network.REGTEST,
            databaseConfig,
        )
        Log.i(LDKTAG, "Successfully created/restored wallet with mnemonic $mnemonic")
    }

    fun getNewAddress(): String {
        return onchainWallet.getAddress(AddressIndex.NEW).address
    }

    fun getBalance(): String {
        return onchainWallet.getBalance().toString()
    }

    fun sync() {
        onchainWallet.sync(
            blockchain = blockchain,
            progress = LogProgress
        )
    }

    fun getHeight(): UInt {
        return blockchain.getHeight()
    }

    fun getBlockHash(height: UInt): String {
        return blockchain.getBlockHash(height)
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun getLdkEntropy(): ByteArray {
        val mnemonic: String = loadMnemonic()
        val bip32RootKey: DescriptorSecretKey = DescriptorSecretKey(
            network = Network.REGTEST,
            mnemonic = mnemonic,
            password = null,
        )
        val derivationPath = DerivationPath("m/535h")
        val child: DescriptorSecretKey = bip32RootKey.derive(derivationPath)
        val entropy: ByteArray = child.secretBytes().toUByteArray().toByteArray()

        Log.i(LDKTAG, "Entropy used for LDK is ${entropy.toHex()}")
        return entropy
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun buildFundingTx(value: Long, script: ByteArray): ByteArray {
        sync()
        val scriptListUByte: List<UByte> = script.toUByteArray().asList()
        val outputScript = Script(scriptListUByte)
        val (psbt, _) = TxBuilder()
            .addRecipient(outputScript, value.toULong())
            .feeRate(4.0F)
            .finish(onchainWallet)
        sign(psbt)
        val rawTx = psbt.extractTx().toUByteArray().toByteArray()
        Log.i(LDKTAG, "The raw funding tx is ${rawTx.toHex()}")
        return rawTx
    }

    private fun sign(psbt: PartiallySignedBitcoinTransaction) {
        onchainWallet.sign(psbt)
    }

    fun broadcast(signedPsbt: PartiallySignedBitcoinTransaction): String {
        val blockchain = createBlockchain()
        blockchain.broadcast(signedPsbt)
        return signedPsbt.txid()
    }

    private fun createBlockchain(): Blockchain {
        val esploraURL: String = "http://10.0.2.2:3002"
        val blockchainConfig = BlockchainConfig.Esplora(EsploraConfig(esploraURL, null, 5u, 20u, null))
        return Blockchain(blockchainConfig)
    }

    private fun loadMnemonic(): String {
        try {
            return File(Global.homeDir + "/" + "mnemonic.txt").readText()
        } catch (e: Throwable) {
            // if mnemonic doesn't exist, generate one and save it
            Log.i(LDKTAG, "No mnemonic backup, we'll create a new wallet")
            val mnemonic = generateMnemonic(WordCount.WORDS12)
            File(Global.homeDir + "/" + "mnemonic.txt").writeText(mnemonic)
            return mnemonic
        }
    }

    fun recoveryPhrase(): String {
        return File(Global.homeDir + "/" + "mnemonic.txt").readText()
    }

    object LogProgress: Progress {
        override fun update(progress: Float, message: String?) {
            Log.d(LDKTAG, "updating wallet $progress $message")
        }
    }
}
