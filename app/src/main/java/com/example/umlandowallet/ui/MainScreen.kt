package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.umlandowallet.data.Screen

@Composable
fun MainScreen(navController: NavController) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 50.dp)
    ) {
        Button(onClick = {
            navController.navigate(Screen.ConnectPeerScreen.route)
        }) {
            Text(text = "Connect Peer")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(Screen.ListPeersScreen.route)
        }) {
            Text(text = "List Peers")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            navController.navigate(Screen.VersionScreen.route)
        }) {
            Text(text = "Version")
        }
    }
}