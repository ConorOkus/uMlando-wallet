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
import com.example.umlandowallet.Global.channelManager
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

                val invoice = Bolt11Invoice.from_str(recipientInvoice)
                val invoiceResult = (invoice as Result_Bolt11InvoiceParseOrSemanticErrorZ.Result_Bolt11InvoiceParseOrSemanticErrorZ_OK).res
                val paymentParams = UtilMethods.payment_parameters_from_invoice(invoiceResult)
                val paymentParamsResult = (paymentParams as Result_C3Tuple_ThirtyTwoBytesRecipientOnionFieldsRouteParametersZNoneZ.Result_C3Tuple_ThirtyTwoBytesRecipientOnionFieldsRouteParametersZNoneZ_OK).res

                val paymentHash = paymentParamsResult._a
                val recipientOnion = paymentParamsResult._b
                val paymentId = paymentParamsResult._a
                val routeParams = paymentParamsResult._c
                val res = channelManager?.send_payment(paymentHash, recipientOnion, paymentId, routeParams, Retry.attempts(5))

                if (res != null) {
                    if(res.is_ok) {
                        Log.i(LDKTAG, "Payment sent successfully")
                    } else {
                        Log.e(LDKTAG, "Payment failed")
                    }
                }


            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
            Text(text = "Send")
        }
    }
}