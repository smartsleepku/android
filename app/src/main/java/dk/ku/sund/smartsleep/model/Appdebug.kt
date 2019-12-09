package dk.ku.sund.smartsleep.model

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import dk.ku.sund.smartsleep.manager.db
import java.util.*

data class Appdebug (
    var id: Int?,
    var time: Date?,
    var type: String?
) {
    companion object {
        fun initializeDatabase(db: SQLiteDatabase) {
            val createTableString = "create table if not exists appdebug(" +
                    "id integer primary key autoincrement," +
                    "\"time\" integer not null unique," +
                    "\"type\" varchar(12) not null)"
            db.execSQL(createTableString)
        }
    }

    constructor(cursor: Cursor): this(null, null, null) {
        id = cursor.getInt(cursor.getColumnIndex("id"))
        time = Date(cursor.getLong(cursor.getColumnIndex("time")) * 1000)
        type = cursor.getString(cursor.getColumnIndex("type"))
    }

    fun save() {
        if (id == null) {
            val insertStatementString = "insert or replace into appdebug (\"time\", \"type\") " +
                    "values (?, ?)"
            db?.execSQL(insertStatementString, arrayOf((time ?: Date()).time / 1000, type))
        } else {
            val updateStatementString = "update appdebug set (\"time\" = ?, \"type\" = ?) " +
                    "where id = ?"
            db?.execSQL(updateStatementString, arrayOf(time?.time?.div(1000), type, id))
        }
    }
}