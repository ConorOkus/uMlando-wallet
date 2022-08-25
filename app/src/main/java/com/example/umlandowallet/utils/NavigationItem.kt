package com.example.umlandowallet.utils

import com.example.umlandowallet.R
import com.example.umlandowallet.ui.Screen

sealed class NavigationItem(val route: String, val icon_filled: Int, val icon_outline: Int, val title: String) {
    object Wallet : NavigationItem(
        route = Screen.WalletScreen.route,
        icon_filled = R.drawable.lightning_icon,
        icon_outline = R.drawable.lightning_icon,
        title = "Wallet"
    )

    object Settings : NavigationItem(
        route = Screen.SettingsScreen.route,
        icon_filled = R.drawable.ic_hicon_menu,
        icon_outline = R.drawable.ic_hicon_menu,
        title = "Settings"
    )
}
