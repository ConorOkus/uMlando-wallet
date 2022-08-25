package com.example.umlandowallet.ui

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.Global
import org.bitcoindevkit.*
import java.io.File

@Composable
fun Wallet() {
    val restoreWalletStatusMessage = remember { mutableStateOf("") }
    val syncWalletStatusMessage = remember { mutableStateOf("") }
    val (mnemonic, setMnemonic) = remember { mutableStateOf("") }
    val (address, setAddress) = remember { mutableStateOf("") }
    val (balance, setBalance) = remember { mutableStateOf("") }

    Button(
        onClick = {
            val keys = generateExtendedKey(Network.TESTNET, WordCount.WORDS12, null)

            val descriptor: String = createDescriptor(keys)
            val changeDescriptor: String = createChangeDescriptor(keys)

            val databaseConfig = DatabaseConfig.Memory

            Global.wallet = Wallet(
                descriptor,
                changeDescriptor,
                Network.TESTNET,
                databaseConfig,
            )

            File(Global.homeDir + "/" + "mnemonic").writeText(keys.mnemonic);

            setMnemonic(keys.mnemonic)
        },
    ) {
        Text(text = "Create Wallet")
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (mnemonic != "") {
        Text(text = mnemonic)
        Spacer(modifier = Modifier.height(8.dp))
    }
    Button(
        onClick = {
            val mnemonic = File(Global.homeDir + "/" + "mnemonic").readText();

            val keys = restoreExtendedKey(Network.TESTNET, mnemonic, null)
            val descriptor: String = createDescriptor(keys)
            val changeDescriptor: String = createChangeDescriptor(keys)

            val databaseConfig = DatabaseConfig.Memory

            Global.wallet = Wallet(
                descriptor,
                changeDescriptor,
                Network.TESTNET,
                databaseConfig,
            )

            restoreWalletStatusMessage.value = "Successfully restored wallet"
        },
    ) {
        Text(text = "Restore Wallet")
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (restoreWalletStatusMessage.value != "") {
        Text(text = restoreWalletStatusMessage.value)
        Spacer(modifier = Modifier.height(8.dp))
    }
    Button(
        onClick = {
            val blockchain = createBlockchain()
            Global.wallet!!.sync(blockchain, LogProgress)

            syncWalletStatusMessage.value = "Wallet synced"
        },
    ) {
        Text(text = "Sync Wallet")
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (syncWalletStatusMessage.value != "") {
        Text(text = syncWalletStatusMessage.value)
        Spacer(modifier = Modifier.height(8.dp))
    }
    Button(
        onClick = {
            setAddress(Global.wallet!!.getAddress(AddressIndex.NEW).address)
            Log.d(TAG, "wallet address + ${Global.wallet!!.getAddress(AddressIndex.NEW).address}")
        },
    ) {
        Text(text = "Get Address")
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (address != "") {
        SelectionContainer {
            Text(text = address)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
    Button(
        onClick = {
            setBalance(Global.wallet!!.getBalance().toString())
        },
    ) {
        Text(text = "Get Balance")
    }
    Spacer(modifier = Modifier.height(8.dp))
    if (balance != "") {
        Text(text = balance)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

private const val TAG = "Wallet"

object LogProgress: Progress {
    override fun update(progress: Float, message: String?) {
        Log.d(TAG, "updating wallet $progress $message")
    }
}

private fun createDescriptor(keys: ExtendedKeyInfo): String {
    Log.i(TAG,"Descriptor for receive addresses is wpkh(${keys.xprv}/84'/1'/0'/0/*)")
    return ("wpkh(${keys.xprv}/84'/1'/0'/0/*)")
}

private fun createChangeDescriptor(keys: ExtendedKeyInfo): String {
    Log.i(TAG, "Descriptor for change addresses is wpkh(${keys.xprv}/84'/1'/0'/1/*)")
    return ("wpkh(${keys.xprv}/84'/1'/0'/1/*)")
}

private fun createBlockchain(): Blockchain {
    val electrumURL: String = "ssl://electrum.blockstream.info:60002"

    val blockchainConfig =
        BlockchainConfig.Electrum(ElectrumConfig(electrumURL, null, 5u, null, 10u))

    return Blockchain(blockchainConfig)
}


