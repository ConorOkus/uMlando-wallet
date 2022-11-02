package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .padding(top = 48.dp)
            .fillMaxSize()
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

        Button(
            onClick = {
                navController.navigate(Screen.NodeIdScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff0f0f0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .size(width = 400.dp, height = 70.dp)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp)
        ) {
            Text(
                text = "Node ID",
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

        Button(
            onClick = {
                navController.navigate(Screen.ListPeersScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff0f0f0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .size(width = 400.dp, height = 70.dp)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp)
        ) {
            Text(
                text = "List peers",
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

        Button(
            onClick = {
                navController.navigate(Screen.ConnectPeerScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff0f0f0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .size(width = 400.dp, height = 70.dp)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp)
        ) {
            Text(
                text = "Connect to a peer",
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

        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff0f0f0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .size(width = 400.dp, height = 70.dp)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp)
        ) {
            Text(
                text = "Open a channel",
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

        Button(
            onClick = {
                navController.navigate(Screen.RecoveryPhraseScreen.route) {
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route)
                    }
                    launchSingleTop = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xfff0f0f0)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .size(width = 400.dp, height = 70.dp)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp)
        ) {
            Text(
                text = "Recovery phrase",
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
}
