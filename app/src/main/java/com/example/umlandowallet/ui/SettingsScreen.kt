package com.example.umlandowallet.ui

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.umlandowallet.R
import com.example.umlandowallet.OnchainWallet
import com.example.umlandowallet.utils.LDKTAG

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .padding(top = 48.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Title
        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        )
        Text(
            text = "Lightning Node",
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
        )
        Divider(
            color = Color(0xffababab),
            thickness = 1.dp,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp)
        )

        // Node ID
        SettingButton(
            label = "Node ID",
            onClick = {
                navController.navigate(Screen.NodeIdScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            }
        )

        // List peers
        SettingButton(
            label = "List peers",
            onClick = {
                navController.navigate(Screen.ListPeersScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            }
        )

        // Connect to a peer
        SettingButton(
            label = "Connect to a peer",
            onClick = {
                navController.navigate(Screen.ConnectPeerScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            }
        )

        // Open a channel
        SettingButton(
            label = "Open a channel",
            onClick = {
                navController.navigate(Screen.OpenChannelScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            }
        )

        // List channels
        SettingButton(
            label = "List channels",
            onClick = {
                navController.navigate(Screen.ListChannelsScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            }
        )

        // Send payment
        SettingButton(
            label = "Send payment",
            onClick = {
                navController.navigate(Screen.SendPaymentScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            }
        )

        // Receive payment
        SettingButton(
            label = "Receive payment",
            onClick = {
                navController.navigate(Screen.ReceivePaymentScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            }
        )

        // Close channel
        SettingButton(
            label = "Close channel",
            onClick = {
                navController.navigate(Screen.CloseChannelScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            }
        )

        Text(
            text = "Onchain Wallet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xff1f0208),
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, top = 36.dp, bottom = 8.dp)
        )
        Divider(
            color = Color(0xffababab),
            thickness = 1.dp,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp)
        )

        // Recovery phrase
        SettingButton(
            label = "Recovery phrase",
            onClick = {
                navController.navigate(Screen.RecoveryPhraseScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            }
        )

        // Get new address
        SettingButton(
            label = "Get new address",
            onClick = { Log.i(LDKTAG, "New bitcoin address: ${OnchainWallet.getNewAddress()}") }
        )

        // Get balance
        SettingButton(
            label = "Get balance",
            onClick = { Log.i(LDKTAG, "On chain balance: ${OnchainWallet.getBalance()}") }
        )
    }
}

@Composable
internal fun SettingButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff0f0f0)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .size(width = 400.dp, height = 70.dp)
            .padding(start = 24.dp, end = 24.dp, top = 24.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Normal,
            color = Color(0xff2f2f2f)

        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            painter = painterResource(id = R.drawable.ic_hicon_right_arrow),
            contentDescription = "Right arrow icon",
            tint = Color(0xff000000)
        )
    }
}
