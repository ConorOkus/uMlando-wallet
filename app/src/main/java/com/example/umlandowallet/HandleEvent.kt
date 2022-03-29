package com.example.umlandowallet

import org.ldk.structs.*

fun handleEvent(event: Event) {
    if (event is Event.PaymentPathSuccessful) {
        println("LDK: payment path successful");
    }
}