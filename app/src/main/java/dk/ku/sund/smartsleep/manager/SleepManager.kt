package dk.ku.sund.smartsleep.manager

import android.util.Log
import androidx.core.content.edit
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.httpPost
import com.google.gson.GsonBuilder
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import dk.ku.sund.smartsleep.model.Sleep
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import java.util.*

fun fetchSleeps(from: Date, to: Date): List<Sleep> {
    val cursor = db?.rawQuery("select * from sleeps " +
            "where time >= ${from.time / 1000} " +
            "and time <= ${to.time / 1000} " +
            "order by time asc", emptyArray())
    cursor ?: return emptyList()
    val sleeps = mutableListOf<Sleep>()
    while (cursor.moveToNext()) {
        sleeps.add(Sleep(cursor))
    }
    cursor.close()
    return sleeps
}

private val mutex = Mutex(false)

fun bulkPostSleep() = runBlocking {
    if (!hasJwt) return@runBlocking
    mutex.lock()
    val prefs = SecuredPreferenceStore.getSharedInstance()
    val lastSync = Date(prefs.getLong("lastSleepSync", 0))
    val fetchTime = Date()
    val sleeps = fetchSleeps(lastSync, fetchTime)
    val result = "/sleep/bulk".httpPost()
        .header(mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${jwt}"
        ))
        .body(gson.toJson(sleeps))
        .awaitStringResult()
    if (result.component2() != null) {
        Log.e("SleepManager", "Failed posting sleeps: ${result.component2().toString()}")
        return@runBlocking
    }
    prefs.edit {
        putLong("lastSleepSync", fetchTime.time)
    }
    mutex.unlock()
}
