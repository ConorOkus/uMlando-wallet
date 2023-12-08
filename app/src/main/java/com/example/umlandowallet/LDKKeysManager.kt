package com.example.umlandowallet

import com.example.umlandowallet.utils.convertToByteArray
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

    fun spend_spendable_outputs(
        descriptors: Array<SpendableOutputDescriptor>,
        outputs: Array<TxOut>,
        changeDestinationScript: ByteArray,
        feerateSatPer1000Weight: Int,
        locktime: Option_u32Z
    ): Result_TransactionNoneZ {
        return inner.spend_spendable_outputs(
            descriptors,
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
    override fun get_destination_script(): Result_CVec_u8ZNoneZ {
        val address = ldkkeysManager!!.wallet.getAddress(AddressIndex.New)
        val res = Result_CVec_u8ZNoneZ.ok(convertToByteArray(address.address.scriptPubkey()))
        if (res.is_ok) {
            return res
        }
        return Result_CVec_u8ZNoneZ.err()
    }

    override fun get_shutdown_scriptpubkey(): Result_ShutdownScriptNoneZ {
        val address = ldkkeysManager!!.wallet.getAddress(AddressIndex.New).address

        return when (val payload: Payload = address.payload()) {
            is Payload.WitnessProgram -> {
                val result = ShutdownScript.new_witness_program(
                    WitnessVersion(payload.version.name.toByte()),
                    payload.program.toUByteArray().toByteArray()
                )
                (result as Result_ShutdownScriptNoneZ)
            }
            else -> {
                Result_ShutdownScriptNoneZ.err()
            }
        }
    }
}
