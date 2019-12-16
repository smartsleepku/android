package dk.ku.sund.smartsleep.manager

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.github.kittinunf.fuel.httpPost
import dk.ku.sund.smartsleep.model.Sleep
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

fun fetchSleeps(db: SQLiteDatabase?, from: Date, to: Date): List<Sleep> {
    val cursor = db?.rawQuery("select * from sleeps " +
            "where time > ${from.time / 1000} " +
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

fun deleteOldSleeps(db: SQLiteDatabase?, to: Date) {
    db?.execSQL("delete from sleeps " +
            "where time <= ${to.time / 1000} ")
}

fun bulkPostSleep() = runBlocking {
    if (!hasJwt) return@runBlocking
    Log.i("DatabaseMutex", "SleepManager: mutex = $dbMutex")
    dbMutex.withLock {
        val db = acquireDatabase()
        try {
            val lastSync = Date((load("lastSleepSync", String::class.java) ?: "0").toLong())
            val fetchTime = Date()
            val sleeps = fetchSleeps(db, lastSync, fetchTime)
            Log.i("SleepManager", "Fetched sleeps: $sleeps")
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
            deleteOldSleeps(db, fetchTime)
            store("lastSleepSync", "${fetchTime.time}")
        } catch (e: Exception) {
            Log.e("SleepManager", e.stackTrace.joinToString("\n"))
        } finally {
            releaseDatabase()
        }
    }
}
