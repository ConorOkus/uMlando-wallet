package com.example.umlandowallet

import android.util.Log
import com.example.umlandowallet.utils.LDKTAG
import com.example.umlandowallet.utils.toHex
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
        val bip32ExtendedRootKey = DescriptorSecretKey(Network.REGTEST, Mnemonic.fromString(mnemonic), null)
        val bip84ExternalDescriptor: Descriptor = Descriptor.newBip84(bip32ExtendedRootKey, KeychainKind.EXTERNAL, Network.REGTEST)
        val bip84InternalDescriptor: Descriptor = Descriptor.newBip84(bip32ExtendedRootKey, KeychainKind.INTERNAL, Network.REGTEST)

        val databaseConfig = DatabaseConfig.Memory

        onchainWallet = Wallet(
            bip84InternalDescriptor,
            bip84ExternalDescriptor,
            Network.REGTEST,
            databaseConfig,
        )

        Log.i(LDKTAG, "Successfully created/restored wallet with mnemonic $mnemonic")
    }

    fun getNewAddress(): String {
        return onchainWallet.getAddress(AddressIndex.New).address
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
        val mnemonic = loadMnemonic()
        val bip32RootKey = DescriptorSecretKey(
            network = Network.REGTEST,
            mnemonic = Mnemonic.fromString(mnemonic),
            password = null,
        )
        val derivationPath = DerivationPath("m/535h")
        val child = bip32RootKey.derive(derivationPath)
        val entropy = child.secretBytes().toUByteArray().toByteArray()

        Log.i(LDKTAG, "Entropy used for LDK is ${entropy.toHex()}")
        return entropy
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun buildFundingTx(value: Long, script: ByteArray): Transaction {
        sync()
        val scriptListUByte: List<UByte> = script.toUByteArray().asList()
        val outputScript = Script(scriptListUByte)
        val (psbt, _) = TxBuilder()
            .addRecipient(outputScript, value.toULong())
            .feeRate(4.0F)
            .finish(onchainWallet)
        sign(psbt)
        val rawTx = psbt.extractTx().serialize().toUByteArray().toByteArray()
        Log.i(LDKTAG, "The raw funding tx is ${rawTx.toHex()}")
        return psbt.extractTx()
    }

    private fun sign(psbt: PartiallySignedTransaction) {
        onchainWallet.sign(psbt, null)
    }

    fun broadcast(signedPsbt: PartiallySignedTransaction): String {
        val blockchain = createBlockchain()
        blockchain.broadcast(signedPsbt.extractTx())
        return signedPsbt.txid()
    }

    fun broadcastRawTx(tx: Transaction) {
        val blockchain = createBlockchain()
        blockchain.broadcast(tx)
        // Should expose txid
        Log.i(LDKTAG, "The raw tx is ${tx.serialize().toUByteArray().toByteArray().toHex()}")
    }

    private fun createBlockchain(): Blockchain {
        val esploraURL = "http://10.0.2.2:3002"
        val blockchainConfig = BlockchainConfig.Esplora(EsploraConfig(esploraURL, null, 5u, 20u, null))
        return Blockchain(blockchainConfig)
    }

    private fun loadMnemonic(): String {
        return try {
            File(Global.homeDir + "/" + "mnemonic.txt").readText()
        } catch (e: Throwable) {
            // if mnemonic doesn't exist, generate one and save it
            Log.i(LDKTAG, "No mnemonic backup, we'll create a new wallet")
            val mnemonic = Mnemonic(WordCount.WORDS12)
            File(Global.homeDir + "/" + "mnemonic.txt").writeText(mnemonic.asString())
            mnemonic.asString()
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
