package dk.ku.sund.smartsleep.manager

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import dk.ku.sund.smartsleep.model.Night
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

open class NightGeneratorUpdateHolder(var total: Long = 0, var current: Long = 0, var done: Boolean = false) {
    open fun update(db: SQLiteDatabase?) {}
}

private fun isDateInWeekend(date: Date): Boolean {
    val cal = Calendar.getInstance()
    cal.time = date
    val day = cal.get(Calendar.DAY_OF_WEEK)
    return day == Calendar.SATURDAY || day == Calendar.SUNDAY
}

fun nightThresholds(date: Date): Pair<Date, Date> {
    val morningHour: Int
    val morningMinute: Int
    val eveningHour: Int
    val eveningMinute: Int
    val cal = Calendar.getInstance()
    if (isDateInWeekend(date)) {
        cal.time = currentConfiguration.weekendMorning
        morningHour = cal.get(Calendar.HOUR_OF_DAY)
        morningMinute = cal.get(Calendar.MINUTE)
        cal.time = currentConfiguration.weekendEvening
        eveningHour = cal.get(Calendar.HOUR_OF_DAY)
        eveningMinute = cal.get(Calendar.MINUTE)
    } else {
        cal.time = currentConfiguration.weekdayMorning
        morningHour = cal.get(Calendar.HOUR_OF_DAY)
        morningMinute = cal.get(Calendar.MINUTE)
        cal.time = currentConfiguration.weekdayEvening
        eveningHour = cal.get(Calendar.HOUR_OF_DAY)
        eveningMinute = cal.get(Calendar.MINUTE)
    }
    cal.time = date
    cal.set(Calendar.HOUR_OF_DAY, eveningHour)
    cal.set(Calendar.MINUTE, eveningMinute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    if (cal.time > date) {
        cal.add(Calendar.DATE, -1)
    }
    val start = cal.time
    cal.time = date
    cal.set(Calendar.HOUR_OF_DAY, morningHour)
    cal.set(Calendar.MINUTE, morningMinute)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    if (cal.time < start) {
        cal.add(Calendar.DATE, 1)
    }
    val end = cal.time
    return Pair(start, end)
}

fun fetchNights(db: SQLiteDatabase?): List<Night> {
    val cursor = db?.rawQuery("select * from nights order by \"from\" desc", emptyArray())
    cursor ?: return emptyList<Night>()
    val nights = mutableListOf<Night>()
    while (cursor.moveToNext()) {
        nights.add(Night(cursor))
    }
    cursor.close()
    return nights
}

fun countNights(): Int = runBlocking {
    Log.i("DatabaseMutex", "NightManager-countNights: mutex = $dbMutex")
    dbMutex.withLock {
        val db = acquireDatabase()
        try {
            val cursor = db?.rawQuery("select count(1) from nights", emptyArray())
            cursor ?: return@runBlocking 0
            if (cursor.moveToNext() == false) {
                cursor.close()
                return@runBlocking 0
            }
            val result = cursor.getInt(0)
            cursor.close()
            return@runBlocking result
        } finally {
            releaseDatabase()
        }
    }
}

fun fetchOneNight(date: Date): Night? = runBlocking {
    Log.i("DatabaseMutex", "NightManager-fetchOneNight: mutex = $dbMutex")
    dbMutex.withLock {
        val db = acquireDatabase()
        try {
            val pair = nightThresholds(date)
            val cursor = db?.rawQuery("select * from nights where \"from\" = ${pair.first.time / 1000}", emptyArray())
            cursor ?: return@runBlocking null
            var night: Night? = null
            if(cursor.moveToNext()) {
                night = Night(cursor)
            }
            cursor.close()
            return@runBlocking night
        } finally {
            releaseDatabase()
        }
    }
}

fun purgeNights(db: SQLiteDatabase?) {
    db?.execSQL("delete from nights")
}

fun generateNights(updateHolder: NightGeneratorUpdateHolder) = runBlocking {
    Log.i("DatabaseMutex", "NightManager-generateNights: mutex = $dbMutex")
    dbMutex.withLock {
        val db = acquireDatabase()
        Log.i("DatabaseMutex", "NightManager-generateNights: db = $db")
        try {
            purgeNights(db)
            Log.i("DatabaseMutex", "NightManager-generateNights: nights purged")
            var now = Date()
            var from: Date
            var to: Date
            val first = fetchFirstRestTime(db)
            updateHolder.total = now.time - first.time
            updateHolder.update(db)
            Log.i("DatabaseMutex", "NightManager-generateNights: update 1")
            val cal = Calendar.getInstance()
            do {
                val pair = nightThresholds(now)
                from = pair.first
                to = pair.second
                cal.time = now
                cal.add(Calendar.DATE, -1)
                Log.i("NightManager", "generating night from ${from} to ${to}...")
                now = cal.time
                val night = Night(
                    null,
                    from,
                    to,
                    fetchUnrestCount(db, from, to),
                    fetchLongestRest(db, from, to),
                    fetchTotalUnrest(db, from, to)
                )
                night.save(db)
                Log.i("DatabaseMutex", "NightManager-generateNights: save loop")
                updateHolder.current += 60 * 60 * 24 * 1000
                updateHolder.update(db)
                Log.i("DatabaseMutex", "NightManager-generateNights: update loop")
            } while (from > first)
        } catch (e: Exception) {
            Log.e("NightManager", e.stackTrace.joinToString("\n"))
        } finally {
            updateHolder.done = true
            updateHolder.update(db)
            releaseDatabase()
        }
    }
    Log.i("DatabaseMutex", "NightManager-generateNights: mutex = $dbMutex")
}