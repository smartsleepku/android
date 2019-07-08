package dk.ku.sund.smartsleep.manager

import dk.ku.sund.smartsleep.model.Rest
import dk.ku.sund.smartsleep.model.Sleep
import java.util.*

fun updateLatestRest(sleep: Sleep) {
    val cursor = db?.rawQuery("select * from rests order by startTime desc limit 1", emptyArray())
    cursor ?: return
    var rest: Rest? = null
    if (cursor.moveToNext()) {
        rest = Rest(cursor)
    }
    cursor.close()

    if (rest == null) {
        rest = Rest(null, sleep.sleeping, Date(), null)
        rest.save()
    } else if (rest.resting!! != sleep.sleeping) {
        rest.endTime = sleep.time
        rest.save()
        rest = Rest(null, sleep.sleeping, sleep.time, null)
        rest.save()
    }
}

fun fetchFirstRestTime(): Date {
    val cursor = db?.rawQuery("select min(startTime) from rests", emptyArray())
    if (cursor?.moveToNext() ?: false == false) {
        cursor?.close()
        return Date()
    }
    val min = cursor!!.getLong(0)
    cursor.close()
    if (min == 0L) return Date()
    return Date(min)
}

fun fetchLongestRest(from: Date, to: Date): Long {
    val queryStatement = "select max(cast((min(endTime,?) - max(startTime,?)) as integer)) " +
            "from rests " +
            "where endTime > ? and startTime < ? " +
            "and resting = 1"
    val cursor = db?.rawQuery(queryStatement, arrayOf("${to.time}", "${from.time}", "${from.time}", "${to.time}"))
    if (cursor?.moveToNext() ?: false == false) {
        cursor?.close()
        return 0
    }
    val result = cursor!!.getLong(0)
    cursor.close()
    return result
}

fun fetchUnrestCount(from: Date, to: Date): Int {
    val queryStatement = "select count(1) " +
            "from rests " +
            "where endTime > ? and startTime < ? " +
            "and resting = 0"
    val cursor = db?.rawQuery(queryStatement, arrayOf("${from.time}", "${to.time}"))
    if (cursor?.moveToNext() ?: false == false) {
        cursor?.close()
        return 0
    }
    val result = cursor!!.getInt(0)
    cursor.close()
    return result
}

fun fetchTotalUnrest(from: Date, to: Date): Long {
    val queryStatement = "select sum(cast((min(endTime,?) - max(startTime,?)) as integer)) " +
            "from rests " +
            "where endTime > ? and startTime < ? " +
            "and resting = 0"
    val cursor = db?.rawQuery(queryStatement, arrayOf("${to.time}", "${from.time}", "${from.time}", "${to.time}"))
    if (cursor?.moveToNext() ?: false == false) {
        cursor?.close()
        return 0
    }
    val result = cursor!!.getLong(0)
    cursor.close()
    return result
}