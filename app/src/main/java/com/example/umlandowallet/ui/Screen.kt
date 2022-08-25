package com.example.umlandowallet.ui

sealed class Screen(val route: String) {
    object WalletScreen : Screen("wallet_screen")
    object SettingsScreen : Screen("settings_screen")
    object NodeIdScreen : Screen("node_id_screen")
    object ListPeersScreen : Screen("list_peers")
    object ConnectPeerScreen : Screen("connect_peer")
    object RecoveryPhraseScreen : Screen("recovery_phrase")
}
