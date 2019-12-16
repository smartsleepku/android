package dk.ku.sund.smartsleep.manager

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpPost
import dk.ku.sund.smartsleep.model.Heartbeat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

const val HEARTBEAT_INTERVAL = (5 * 60 * 1000).toLong() // 10 minutes

fun fetchHeartbeats(db: SQLiteDatabase?, from: Date, to: Date): List<Heartbeat> {
    val cursor = db?.rawQuery("select * from heartbeats " +
            "where time > ${from.time / 1000} " +
            "and time <= ${to.time / 1000} " +
            "order by time asc", emptyArray())
    cursor ?: return emptyList()
    val heartbeats = mutableListOf<Heartbeat>()
    while (cursor.moveToNext()) {
        heartbeats.add(Heartbeat(cursor))
    }
    cursor.close()
    return heartbeats
}

fun deleteOldHeartbeats(db: SQLiteDatabase?, to: Date) {
    db?.execSQL("delete from heartbeats " +
            "where time <= ${to.time / 1000} ")
}

fun bulkPostHeartbeat() : Boolean = runBlocking {
    if (!hasJwt) return@runBlocking false
    Log.i("DatabaseMutex", "HeartbeatManager: mutex = $dbMutex")
    dbMutex.withLock {
        val db = acquireDatabase()
        var retval = false
        try {
            val lastSync = Date((load("lastHeartbeatSync", String::class.java) ?: "0").toLong())
            val fetchTime = Date()
            val heartbeats = fetchHeartbeats(db, lastSync, fetchTime)
            val result = "/heartbeat/bulk".httpPost()
                .header(mapOf(
                    "Content-Type" to "application/json",
                    "Authorization" to "Bearer $jwt"
                ))
                .body(gson.toJson(heartbeats))
                .awaitStringResult()
            if (result.component2() != null) {
                Log.e("SHeartbeat", "Failed posting heartbeats: ${result.component2().toString()}")
                return@runBlocking false
            }
            deleteOldHeartbeats(db, fetchTime)
            store("lastHeartbeatSync", "${fetchTime.time}")
            retval = true
        } catch (e: Exception) {
            Log.e("SHeartbeat", e.stackTrace.joinToString("\n"))
        } finally {
            releaseDatabase()
        }
        return@runBlocking retval
    }
}
