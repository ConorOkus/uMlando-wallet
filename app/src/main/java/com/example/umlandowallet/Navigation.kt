package com.example.umlandowallet

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.umlandowallet.data.Screen
import com.example.umlandowallet.ui.MainScreen

@Composable
fun navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
//        composable(route = Screen.MainScreen.route) {
//            MainScreen(navController = navController)
//        }
    }
}