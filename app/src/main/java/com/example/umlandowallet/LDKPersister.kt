package com.example.umlandowallet

import android.util.Log
import com.example.umlandowallet.utils.LDKTAG
import com.example.umlandowallet.utils.toHex
import com.example.umlandowallet.utils.write
import org.ldk.enums.ChannelMonitorUpdateStatus
import org.ldk.structs.*

// To create a Persister for our Channel Monitors we need to provide an object that implements the PersistInterface
// which has 2 functions persist_new_channel & update_persisted_channel
// Consider return ChannelMonitorUpdateStatus::InProgress for async backups
object LDKPersister : Persist.PersistInterface {
    private fun persist(id: OutPoint?, data: ByteArray?) {
        if(id != null && data != null) {
            val identifier = "channels/${id.to_channel_id().toHex()}.bin"
            write(identifier, data)
        }
    }

    override fun persist_new_channel(
        id: OutPoint?,
        data: ChannelMonitor?,
        updateId: MonitorUpdateId?
    ): ChannelMonitorUpdateStatus? {
        return try {
            if (data != null && id != null) {
                Log.i(LDKTAG, "persist_new_channel: ${id.to_channel_id().toHex()}")
                persist(id, data.write())
            }
            ChannelMonitorUpdateStatus.LDKChannelMonitorUpdateStatus_Completed
        } catch (e: Exception) {
            Log.i(LDKTAG, "Failed to write to file: ${e.message}")
            ChannelMonitorUpdateStatus.LDKChannelMonitorUpdateStatus_UnrecoverableError
        }
    }

    // Consider returning ChannelMonitorUpdateStatus::InProgress for async backups
    override fun update_persisted_channel(
        id: OutPoint?,
        update: ChannelMonitorUpdate?,
        data: ChannelMonitor?,
        updateId: MonitorUpdateId
    ): ChannelMonitorUpdateStatus? {
        return try {
            if (data != null && id != null) {
                Log.i(LDKTAG, "update_persisted_channel: ${id.to_channel_id().toHex()}")
                persist(id, data.write())
            }
            ChannelMonitorUpdateStatus.LDKChannelMonitorUpdateStatus_Completed
        } catch (e: Exception) {
            Log.i(LDKTAG, "Failed to write to file: ${e.message}")
            ChannelMonitorUpdateStatus.LDKChannelMonitorUpdateStatus_UnrecoverableError

        }

    }
}