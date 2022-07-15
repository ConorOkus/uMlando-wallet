package com.example.umlandowallet.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.umlandowallet.Global
import com.example.umlandowallet.data.Screen
import com.example.umlandowallet.data.remote.Service
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ldk.structs.NetworkGraph
import org.ldk.structs.RapidGossipSync
import org.ldk.structs.Result_NetworkGraphDecodeErrorZ
import org.ldk.structs.Result_u32GraphSyncErrorZ
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
fun RapidGossipSyncScreen() {
    val service = Service.create()

    val coroutineScope = rememberCoroutineScope()
    val (snapshot, setSnapshot) = remember { mutableStateOf<ByteArray?>(null) }

    val getLatestRapidSnapshotOnClick: () -> Unit = {
        coroutineScope.launch {
            val snapshot = service.getLatestRapidSnapshot()
            setSnapshot(snapshot)
        }
    }

    val syncGossipData: () -> Unit = {
        val serializedGraph = File(Global.homeDir+ "/" + Global.prefixNetworkGraph).readBytes()
        val readResult = NetworkGraph.read(serializedGraph, Global.logger)
        if (readResult is Result_NetworkGraphDecodeErrorZ.Result_NetworkGraphDecodeErrorZ_OK) {
            Global.router = readResult.res

            val rapidSync = RapidGossipSync.of(Global.router)

            println("Applying rapid sync data...")
            val startTime = System.currentTimeMillis()
            val timestamp = rapidSync.update_network_graph(snapshot)
            val elapsedTime = System.currentTimeMillis() - startTime
            if(timestamp is Result_u32GraphSyncErrorZ.Result_u32GraphSyncErrorZ_OK) {
                println("Applied rapid sync data: Time: ${timestamp.res} ${TimeUnit.MILLISECONDS.toSeconds(elapsedTime).toDouble()}s")
            }

            if(timestamp is Result_u32GraphSyncErrorZ.Result_u32GraphSyncErrorZ_Err) {
                println("Error applying rapid sync data: Time: ${timestamp.err} ${TimeUnit.MILLISECONDS.toSeconds(elapsedTime).toDouble()}s")
            }
        }
    }

    val getGraphSize: () -> Unit = {
       println("Measuring graph size…")
        val startTime = System.currentTimeMillis()
        val graphBytes = Global.router!!.write()
        val elapsedTime = System.currentTimeMillis() - startTime
        println("Network graph size: ${graphBytes.count()} " +
                " Time: ${TimeUnit.MILLISECONDS.toSeconds(elapsedTime).toDouble()}s")
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 50.dp)
    ) {
        Button(onClick = getLatestRapidSnapshotOnClick) {
            Text(text = "Fetch Gossip Data")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = syncGossipData) {
            Text(text = "Sync Gossip Data")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = getGraphSize) {
            Text(text = "Graph Size")
        }
//        Text(text = "${snapshot!!.count()} bytes")
    }
}