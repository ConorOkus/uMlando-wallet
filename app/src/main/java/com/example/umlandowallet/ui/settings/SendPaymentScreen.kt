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
import com.example.umlandowallet.Global.channelManagerConstructor
import com.example.umlandowallet.utils.LDKTAG
import org.ldk.structs.*

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
                val invoiceResult = Bolt11Invoice.from_str(recipientInvoice)
                if(invoiceResult is Result_Bolt11InvoiceParseOrSemanticErrorZ.Result_Bolt11InvoiceParseOrSemanticErrorZ_Err) {
                    Log.i(LDKTAG, "Unable to parse invoice ${invoiceResult.err}")
                }
                val invoice =
                    (invoiceResult as Result_Bolt11InvoiceParseOrSemanticErrorZ.Result_Bolt11InvoiceParseOrSemanticErrorZ_OK).res

                if (invoiceResult.is_ok) {
                    Log.i(LDKTAG, "Invoice parsed successfully")
                } else {
                    Log.i(LDKTAG, "Unable to parse invoice")
                }

                val invoicePaymentResult = UtilMethods.pay_invoice(
                    invoice,
                    Retry.attempts(6),
                    channelManagerConstructor!!.channel_manager
                )
                if (invoicePaymentResult.is_ok) {
                    Log.i(LDKTAG, "Payment successful")
                }

                val error = invoicePaymentResult as? Result_ThirtyTwoBytesPaymentErrorZ.Result_ThirtyTwoBytesPaymentErrorZ_Err
                val invoiceError = error?.err as? PaymentError.Invoice
                if (invoiceError != null) {
                    Log.i(LDKTAG, "Payment failed: $invoiceError")
                }

                val sendingError = error?.err as? PaymentError.Sending
                if (sendingError != null) {
                    val paymentAllFailedResendSafe = sendingError.sending as? PaymentSendFailure.AllFailedResendSafe
                    if (paymentAllFailedResendSafe != null) {
                        Log.i(LDKTAG, "Payment failed: $paymentAllFailedResendSafe")
                    }

                    val paymentParameterError = sendingError.sending as? PaymentSendFailure.ParameterError
                    if (paymentParameterError != null) {
                        Log.i(LDKTAG, "Payment failed: $paymentParameterError")
                    }

                    val paymentPartialFailure = sendingError.sending as? PaymentSendFailure.PartialFailure
                    if (paymentPartialFailure != null) {
                        Log.i(LDKTAG, "Payment failed: $paymentPartialFailure")
                    }

                    val paymentPathParameterError = sendingError.sending as? PaymentSendFailure.PathParameterError
                    if (paymentPathParameterError != null) {
                        Log.i(LDKTAG, "Payment failed: $paymentPathParameterError")
                    }

                    val paymentDuplicateError = sendingError.sending as? PaymentSendFailure.DuplicatePayment
                    if (paymentDuplicateError != null) {
                        Log.i(LDKTAG, "Payment failed: $paymentDuplicateError")
                    }

                    Log.i(LDKTAG, "Payment failed with some unknown error")
                }


            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(text = "Send")
        }
    }
}