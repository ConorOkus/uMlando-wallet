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
            val mnemonic: String = generateMnemonic(WordCount.WORDS12)
            val descriptorSecretKey = DescriptorSecretKey(Network.REGTEST, mnemonic, null)

            val derivedKey = descriptorSecretKey.derive(DerivationPath("m/84h/1h/0h"))
            val externalDescriptor = "wpkh(${derivedKey.extend(DerivationPath("m/0")).asString()})"
            val internalDescriptor = "wpkh(${derivedKey.extend(DerivationPath("m/1")).asString()})"

            val databaseConfig = DatabaseConfig.Memory

            Global.wallet = Wallet(
                internalDescriptor,
                externalDescriptor,
                Network.REGTEST,
                databaseConfig,
            )

            File(Global.homeDir + "/" + "mnemonic").writeText(mnemonic);

            setMnemonic(mnemonic)
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

            val descriptorSecretKey = DescriptorSecretKey(Network.REGTEST, mnemonic, null)

            val derivedKey = descriptorSecretKey.derive(DerivationPath("m/84h/1h/0h"))
            val externalDescriptor = "wpkh(${derivedKey.extend(DerivationPath("m/0")).asString()})"
            println("externalDescriptor: $externalDescriptor")
            val internalDescriptor = "wpkh(${derivedKey.extend(DerivationPath("m/1")).asString()})"

            val databaseConfig = DatabaseConfig.Memory

            Global.wallet = Wallet(
                internalDescriptor,
                externalDescriptor,
                Network.REGTEST,
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
            Global.blockchain?.let { Global.wallet!!.sync(it, LogProgress) }

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
            println(Global.wallet!!.getBalance().toString())
            setBalance(Global.wallet!!.getBalance().total.toString())
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


