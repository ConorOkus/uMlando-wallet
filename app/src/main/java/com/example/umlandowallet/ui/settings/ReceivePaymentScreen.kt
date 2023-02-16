package com.example.umlandowallet.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umlandowallet.Global.channelManager
import com.example.umlandowallet.Global.keysManager
import com.example.umlandowallet.LDKLogger
import org.ldk.enums.Currency
import org.ldk.structs.Logger
import org.ldk.structs.Option_u64Z
import org.ldk.structs.Result_InvoiceSignOrCreationErrorZ
import org.ldk.structs.Result_InvoiceSignOrCreationErrorZ.Result_InvoiceSignOrCreationErrorZ_OK
import org.ldk.structs.UtilMethods


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceivePaymentScreen() {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val logger: Logger = Logger.new_impl(LDKLogger)

    val description =  "coffee"
    val amtMsat = 100_000_00L
    val invoice: Result_InvoiceSignOrCreationErrorZ = UtilMethods.create_invoice_from_channelmanager(
        channelManager,
        keysManager!!.as_KeysInterface(),
        logger,
        Currency.LDKCurrency_Regtest,
        Option_u64Z.some(amtMsat),
        description,
        300
    )

    val invoiceResult = (invoice as Result_InvoiceSignOrCreationErrorZ_OK).res
    val encodedInvoice = invoiceResult.to_str()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    )
    {
        Text(
            text = "Lightning Invoice",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )
        Text(
            text = encodedInvoice,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)        )
        Button(
            onClick = {
                clipboardManager.setText(AnnotatedString((encodedInvoice)))
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(text = "Copy invoice")
        }
    }
}
