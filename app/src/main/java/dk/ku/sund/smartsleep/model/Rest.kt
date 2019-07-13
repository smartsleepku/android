package dk.ku.sund.smartsleep.model

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import dk.ku.sund.smartsleep.manager.db
import java.util.*

data class Rest(
    var id: Int?,
    var resting: Boolean?,
    var startTime: Date?,
    var endTime: Date?
) {
    companion object {
        fun initializeDatabase(db: SQLiteDatabase) {
            val createTableString = "create table if not exists rests(" +
                    "id integer primary key autoincrement," +
                    "resting integer," +
                    "startTime integer," +
                    "endTime integer)"
            db.execSQL(createTableString)
        }
    }

    constructor(cursor: Cursor): this(null, null, null, null) {
        id = cursor.getInt(cursor.getColumnIndex("id"))
        resting = cursor.getInt(cursor.getColumnIndex("resting")) != 0
        startTime = Date(cursor.getLong(cursor.getColumnIndex("startTime")) * 1000)
        endTime = Date(cursor.getLong(cursor.getColumnIndex("endTime")) * 1000)
    }

    fun save() {
        if (id == null) {
            val insertStatementString = "insert into rests (resting, startTime, endTime) values (?, ?, ?)"
            var resting = 0
            if (this.resting!!) resting = 1
            db?.execSQL(insertStatementString, arrayOf(resting, startTime!!.time / 1000, endTime?.time?.div(1000)))
        } else {
            val updateStatementString = "update rests set resting = ?, startTime = ?, endTime = ? where id = ?"
            var resting = 0
            if (this.resting!!) resting = 1
            db?.execSQL(updateStatementString, arrayOf(resting, startTime!!.time / 1000, endTime?.time?.div(1000), id))
        }
    }
}
