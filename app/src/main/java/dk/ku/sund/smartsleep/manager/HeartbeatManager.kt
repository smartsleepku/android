package dk.ku.sund.smartsleep.manager

import android.content.Context
import java.util.concurrent.TimeUnit
import android.util.Log
import androidx.work.*
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpPost
import dk.ku.sund.smartsleep.model.Heartbeat
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import java.util.*

fun fetchHeartbeats(from: Date, to: Date): List<Heartbeat> {
    val cursor = db?.rawQuery("select * from heartbeats " +
            "where time >= ${from.time / 1000} " +
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

fun deleteOldHeartbeats(to: Date) {
    db?.execSQL("delete from heartbeats " +
            "where time <= ${to.time / 1000} ")
}

private val mutex = Mutex(false)

fun bulkPostHeartbeat() : Boolean = runBlocking {
    var retval = false
    if (!hasJwt) return@runBlocking false
    try {
        mutex.lock()
        val lastSync = Date((load("lastHeartbeatSync", String::class.java) ?: "0").toLong())
        val fetchTime = Date()
        val heartbeats = fetchHeartbeats(lastSync, fetchTime)
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
        deleteOldHeartbeats(fetchTime)
        store("lastHeartbeatSync", "${fetchTime.time}")
        retval = true
        Log.d("SHeartbeat", "Succeded posting heartbeats")
    } catch (e: Exception) {
        Log.e("SHeartbeat", e.stackTrace.joinToString("\n"))
    } finally {
        mutex.unlock()
    }
    return@runBlocking retval
}
