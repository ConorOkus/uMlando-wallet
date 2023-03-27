package com.example.umlandowallet.ui.settings

import android.util.Log
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.umlandowallet.Global
import com.example.umlandowallet.Global.channelManagerConstructor
import com.example.umlandowallet.utils.LDKTAG
import org.ldk.structs.*
import org.ldk.structs.Result_RouteLightningErrorZ.Result_RouteLightningErrorZ_OK


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendPaymentScreen() {
    var recipientInvoice by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
    )
    {
        Text(
            text = "Send payment",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )
        TextField(
            value = recipientInvoice,
            onValueChange = { recipientInvoice = it },
            placeholder = {
                Text("Paste invoice ID")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )
        Button(
            onClick = {
                val parsedInvoice = Invoice.from_str(recipientInvoice)
                if (!parsedInvoice.is_ok) {
                    Log.i(LDKTAG, "Unable to parse invoice")
                }
                val invoice =
                    (parsedInvoice as Result_InvoiceParseOrSemanticErrorZ.Result_InvoiceParseOrSemanticErrorZ_OK).res

                var amountSats: Long = 0

                if (invoice.amount_milli_satoshis() is Option_u64Z.Some) {
                    amountSats = (invoice.amount_milli_satoshis() as Option_u64Z.Some).some * 1000
                }

                if (amountSats == 0L) {
                    Log.i(LDKTAG, "Handle a zero-value invoice")
                    // <Handle a zero-value invoice>
                }

                val isZeroValueInvoice = invoice.amount_milli_satoshis() is Option_u64Z.None

                // If it's a zero invoice and we don't have an amount then don't proceed
                if (isZeroValueInvoice && amountSats == 0L) {
                    Log.i(LDKTAG, "Zero-value invoice must specify an amount")
                }

                // Amount was set but not allowed to set own amount
                if (amountSats > 0 && !isZeroValueInvoice) {
                    Log.i(LDKTAG, "Amount was set but not allowed to set own amount")
                }

                if (channelManagerConstructor!!.channel_manager == null) {
                    Log.i(LDKTAG, "NO invoice payer")
                }

                val res = UtilMethods.pay_invoice(
                    invoice,
                    Retry.attempts(6),
                    channelManagerConstructor!!.channel_manager
                )

                val error = res as? Result_PaymentIdPaymentErrorZ.Result_PaymentIdPaymentErrorZ_Err
                val invoiceError = error?.err as? PaymentError.Invoice

                if (invoiceError != null) {
                    Log.i(LDKTAG, Error(invoiceError.invoice).toString())
                }

                val sendingError = error?.err as? PaymentError.Sending
                if (sendingError != null) {
                    val paymentAllFailedRetrySafe =
                        sendingError.sending as? PaymentSendFailure.AllFailedResendSafe
                    if (paymentAllFailedRetrySafe != null) {
                        Log.i(
                            LDKTAG,
                            Error(paymentAllFailedRetrySafe.all_failed_resend_safe.map { it.toString() }
                                .toString()).toString()
                        )
                    }

                    val paymentParameterError =
                        sendingError.sending as? PaymentSendFailure.ParameterError
                    if (paymentParameterError != null) {
                        Log.i(
                            LDKTAG,
                            Error(paymentParameterError.parameter_error.toString()).toString()
                        )
                    }

                    val paymentPartialFailure =
                        sendingError.sending as? PaymentSendFailure.PartialFailure
                    if (paymentPartialFailure != null) {
                        Log.i(LDKTAG, Error(paymentPartialFailure.toString()).toString())
                    }

                    val paymentPathParameterError =
                        sendingError.sending as? PaymentSendFailure.PathParameterError
                    if (paymentPathParameterError != null) {
                        Log.i(LDKTAG, Error(paymentPartialFailure.toString()).toString())
                    }

                    Log.i(LDKTAG, Error("PaymentError.Sending").toString())
                }

                if (res.is_ok) {
                    Log.d(LDKTAG, "Invoice payment success")
                }

            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(text = "Send")
        }
    }
}