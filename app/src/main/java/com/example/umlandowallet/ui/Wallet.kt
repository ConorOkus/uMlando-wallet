package com.example.umlandowallet.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.umlandowallet.Global
import com.example.umlandowallet.createBlockchain
import com.example.umlandowallet.data.remote.Access
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.bitcoindevkit.*
import java.io.File

@Composable
fun Wallet() {
    val restoreWalletStatusMessage = remember { mutableStateOf("") }
    val syncWalletStatusMessage = remember { mutableStateOf("") }
    val (mnemonic, setMnemonic) = remember { mutableStateOf("") }
    val (address, setAddress) = remember { mutableStateOf("") }
    val (balance, setBalance) = remember { mutableStateOf("") }

    var sendAddress by remember {
        mutableStateOf("")
    }

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
            val relevantTxIdsFromChannelManager: Array<ByteArray> = Global.channelManager!!.as_Confirm()._relevant_txids
            val relevantTxIdsFromChainMonitor: Array<ByteArray> = Global.chainMonitor!!.as_Confirm()._relevant_txids

            val relevantTxIds: Array<ByteArray> = relevantTxIdsFromChannelManager + relevantTxIdsFromChainMonitor

            CoroutineScope(Dispatchers.IO).launch {
                val access = Access.create()
                // Sync BDK wallet
                access.syncWallet(Global.wallet!!, LogProgress)

                // Sync LDK/Lightning
                access.syncTransactionsUnconfirmed(relevantTxIds, Global.channelManager!!, Global.chainMonitor!!)
            }

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

//    Button(
//        onClick = {
//            val amount = 5
//            val feeRate = 0.00000010
//            val psbt = createTransaction(sendAddress, amount.toULong(), feeRate.toFloat())
//
//        },
//    ) {
//        Text(text = "Send")
//    }
//    Column(verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .padding(vertical = 8.dp))
//    {
//        TextField(
//            value = sendAddress,
//            onValueChange = { sendAddress = it },
//            modifier = Modifier.fillMaxWidth()
//        )
//    }
//    Spacer(modifier = Modifier.height(8.dp))
}

fun createTransaction(recipient: String, amount: ULong, feeRate: Float): PartiallySignedBitcoinTransaction {
    return TxBuilder()
        .addRecipient(recipient, amount)
        .feeRate(satPerVbyte = feeRate)
        .finish(Global.wallet!!)
}

fun sign(psbt: PartiallySignedBitcoinTransaction) {
    Global.wallet!!.sign(psbt)
}

fun broadcast(signedPsbt: PartiallySignedBitcoinTransaction): String {
    val blockchain = createBlockchain()
    blockchain.broadcast(signedPsbt)
    return signedPsbt.txid()
}

private const val TAG = "Wallet"

object LogProgress: Progress {
    override fun update(progress: Float, message: String?) {
        Log.d(TAG, "updating wallet $progress $message")
    }
}


