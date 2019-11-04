package dk.ku.sund.smartsleep.model

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import dk.ku.sund.smartsleep.manager.db
import java.util.*

data class Heartbeat (
    var id: Int?,
    var time: Date?
) {
    companion object {
        fun initializeDatabase(db: SQLiteDatabase) {
            val createTableString = "create table if not exists heartbeats(" +
                    "id integer primary key autoincrement," +
                    "\"time\" integer not null unique)"
            db.execSQL(createTableString)
        }
    }

    constructor(cursor: Cursor): this(null, null) {
        id = cursor.getInt(cursor.getColumnIndex("id"))
        time = Date(cursor.getLong(cursor.getColumnIndex("time")) * 1000)
    }

    fun save() {
        if (id == null) {
            val insertStatementString = "insert or replace into heartbeats (\"time\") " +
                    "values (?)"
            db?.execSQL(insertStatementString, arrayOf((time ?: Date()).time / 1000))
        } else {
            val updateStatementString = "update heartbeats set (\"time\" = ?) " +
                    "where id = ?"
            db?.execSQL(updateStatementString, arrayOf(time?.time?.div(1000), id))
        }
    }
}
