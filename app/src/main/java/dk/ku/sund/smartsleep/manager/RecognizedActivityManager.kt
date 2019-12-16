package dk.ku.sund.smartsleep.manager

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpPost
import dk.ku.sund.smartsleep.model.RecognizedActivity
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

private fun fetchRecognizedActivities(db: SQLiteDatabase?, from: Date, to: Date): List<RecognizedActivity> {
    val cursor = db?.rawQuery("select * from activities " +
            "where time > ? " +
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

fun deleteOldActivities(db: SQLiteDatabase?, to: Date) {
    db?.execSQL("delete from activities " +
            "where time <= ${to.time} ")
}

fun postRecognizedActivities() = runBlocking {
    if (!hasJwt) return@runBlocking
    Log.i("DatabaseMutex", "ActivityManager: mutex = $dbMutex")
    dbMutex.withLock {
        val db = acquireDatabase()
        try {
            val lastSync = Date((load("lastActivitySync", String::class.java) ?: "0").toLong())
            val fetchTime = Date()
            val activities = fetchRecognizedActivities(db, lastSync, fetchTime)
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
                deleteOldActivities(db, fetchTime)
                store("lastActivitySync", "${fetchTime.time}")
            }
        } finally {
            releaseDatabase()
        }
    }
}
