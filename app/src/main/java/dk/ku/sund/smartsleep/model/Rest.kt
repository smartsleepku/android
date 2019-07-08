package dk.ku.sund.smartsleep.model

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import dk.ku.sund.smartsleep.manager.db
import java.util.*

data class Rest(
    var id: String?,
    var resting: Boolean?,
    var startTime: Date?,
    var endTime: Date?
) {
    companion object {
        fun initializeDatabase(db: SQLiteDatabase) {
            val createTableString = "create table if not exists rests(" +
                    "id char(24) primary key not null," +
                    "resting integer," +
                    "startTime integer," +
                    "endTime integer)"
            db.execSQL(createTableString)
        }
    }

    constructor(cursor: Cursor): this(null, null, null, null) {
        id = cursor.getString(cursor.getColumnIndex("id"))
        resting = cursor.getInt(cursor.getColumnIndex("resting")) != 0
        startTime = Date(cursor.getLong(cursor.getColumnIndex("startTime")))
        endTime = Date(cursor.getLong(cursor.getColumnIndex("endTime")))
    }

    fun save() {
        if (id == null) { id = UUID.randomUUID().toString().toLowerCase() }
        val insertStatementString = "insert or replace into rests (id, resting, startTime, endTime) values (?, ?, ?, ?)"
        var resting = 0
        if (this.resting!!) resting = 1
        db?.execSQL(insertStatementString, arrayOf("'${id}'", resting, startTime!!.time, endTime?.time))
    }
}
