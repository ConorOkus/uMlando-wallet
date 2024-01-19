package com.example.umlandowallet

import org.bitcoindevkit.*
import org.bitcoindevkit.Wallet
import org.ldk.structs.*
import org.ldk.structs.TxOut
import org.ldk.util.UInt128
import org.ldk.util.WitnessVersion

class LDKKeysManager(seed: ByteArray, startTimeSecs: Long, startTimeNano: Int, wallet: Wallet) {
    var inner: KeysManager
    var wallet: Wallet
    var signerProvider: LDKSignerProvider

    init {
        this.inner = KeysManager.of(seed, startTimeSecs, startTimeNano)
        this.wallet = wallet
        signerProvider = LDKSignerProvider()
        signerProvider.ldkkeysManager = this
    }

    // We drop all occurences of `SpendableOutputDescriptor::StaticOutput` (since they will be
    // spendable by the BDK wallet) and forward any other descriptors to
    // `KeysManager::spend_spendable_outputs`.
    //
    // Note you should set `locktime` to the current block height to mitigate fee sniping.
    // See https://bitcoinops.org/en/topics/fee-sniping/ for more information.
    fun spend_spendable_outputs(
        descriptors: Array<SpendableOutputDescriptor>,
        outputs: Array<TxOut>,
        changeDestinationScript: ByteArray,
        feerateSatPer1000Weight: Int,
        locktime: Option_u32Z
    ): Result_TransactionNoneZ {
        val onlyNonStatic: Array<SpendableOutputDescriptor> = descriptors.filter { it !is SpendableOutputDescriptor.StaticOutput }.toTypedArray()

        return inner.spend_spendable_outputs(
            onlyNonStatic,
            outputs,
            changeDestinationScript,
            feerateSatPer1000Weight,
            locktime,
        )
    }
}

class LDKSignerProvider : SignerProvider.SignerProviderInterface {
    var ldkkeysManager: LDKKeysManager? = null

    override fun generate_channel_keys_id(p0: Boolean, p1: Long, p2: UInt128?): ByteArray {
        return ldkkeysManager!!.inner.as_SignerProvider().generate_channel_keys_id(p0, p1, p2)
    }

    override fun derive_channel_signer(p0: Long, p1: ByteArray?): WriteableEcdsaChannelSigner {
        return ldkkeysManager!!.inner.as_SignerProvider().derive_channel_signer(p0, p1)
    }

    override fun read_chan_signer(p0: ByteArray?): Result_WriteableEcdsaChannelSignerDecodeErrorZ {
        return ldkkeysManager!!.inner.as_SignerProvider().read_chan_signer(p0)
    }

    // We return the destination and shutdown scripts derived by the BDK wallet.
    @OptIn(ExperimentalUnsignedTypes::class)
    override fun get_destination_script(): Result_CVec_u8ZNoneZ {
        val address = ldkkeysManager!!.wallet.getAddress(AddressIndex.New)
        return Result_CVec_u8ZNoneZ.ok(address.address.scriptPubkey().toBytes().toUByteArray().toByteArray())
    }

    // Only applies to cooperative close transactions.
    override fun get_shutdown_scriptpubkey(): Result_ShutdownScriptNoneZ {
        val address = ldkkeysManager!!.wallet.getAddress(AddressIndex.New).address

        return when (val payload: Payload = address.payload()) {
            is Payload.WitnessProgram -> {
                val ver = when (payload.version.name) {
                    in "V0".."V16" -> payload.version.name.substring(1).toIntOrNull() ?: 0
                    else -> 0 // Default to 0 if it doesn't match any "V0" to "V16"
                }

                val result = ShutdownScript.new_witness_program(
                    WitnessVersion(ver.toByte()),
                    payload.program.toUByteArray().toByteArray()
                )
                Result_ShutdownScriptNoneZ.ok((result as Result_ShutdownScriptInvalidShutdownScriptZ.Result_ShutdownScriptInvalidShutdownScriptZ_OK).res)
            }
            else -> {
                Result_ShutdownScriptNoneZ.err()
            }
        }
    }
}

