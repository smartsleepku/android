package dk.ku.sund.smartsleep.manager

import android.util.Log
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpPost
import dk.ku.sund.smartsleep.model.RecognizedActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import java.util.*

private fun fetchRecognizedActivities(from: Date, to: Date): List<RecognizedActivity> {
    val cursor = db?.rawQuery("select * from activities " +
            "where time >= ? " +
            "and time <= ? " +
            "order by time asc", arrayOf("${from.time}", "${to.time}"))
    cursor ?: return emptyList()
    val activities = mutableListOf<RecognizedActivity>()
    while (cursor.moveToNext()) {
        activities.add(RecognizedActivity(cursor))
    }
    cursor.close()
    return activities
}

private val mutex = Mutex(false)

fun postRecognizedActivities() = runBlocking {
    if (!hasJwt) return@runBlocking
    mutex.lock()

    val lastSync = Date((load("lastActivitySync", String::class.java) ?: "0").toLong())
    val fetchTime = Date()
    val activities = fetchRecognizedActivities(lastSync, fetchTime)
    activities.forEach {
        val result = "/activity".httpPost()
            .header(mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer ${jwt}"
            ))
            .body(gson.toJson(it))
            .awaitStringResult()
        if (result.component2() != null) {
            Log.e("RecognizedActivityMgr", "Failed posting activity: ${result.component2().toString()}")
            return@forEach
        }
        store("lastActivitySync", "${fetchTime.time}")
    }
    mutex.unlock()
}
