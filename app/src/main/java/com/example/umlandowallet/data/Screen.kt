package com.example.umlandowallet.data

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object ConnectPeerScreen: Screen("connect_peer_screen")
    object ListPeersScreen: Screen("list_peers_screen")
    object NodeIdScreen: Screen("node_id_screen")


    fun withArgs(vararg args: String) : String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
