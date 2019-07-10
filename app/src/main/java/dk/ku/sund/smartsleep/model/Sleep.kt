package dk.ku.sund.smartsleep.model

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import dk.ku.sund.smartsleep.manager.db
import java.util.*

data class Sleep (
    var id: Int?,
    var time: Date?,
    var sleeping: Boolean?
) {
    companion object {
        fun initializeDatabase(db: SQLiteDatabase) {
            val createTableString = "create table if not exists sleeps(" +
                    "id integer primary key autoincrement," +
                    "\"time\" integer not null unique," +
                    "\"sleeping\" integer not null)"
            db.execSQL(createTableString)
        }
    }

    constructor(cursor: Cursor): this(null, null, null) {
        id = cursor.getInt(cursor.getColumnIndex("id"))
        time = Date(cursor.getLong(cursor.getColumnIndex("time")) * 1000)
        sleeping = cursor.getInt(cursor.getColumnIndex("sleeping")) != 0
    }

    fun save() {
        if (id == null) {
            val insertStatementString = "insert or replace into sleeps (\"time\", sleeping) " +
                    "values (?, ?)"
            db?.execSQL(insertStatementString, arrayOf((time ?: Date()).time / 1000, sleeping))
        } else {
            val updateStatementString = "update sleeps set (\"time\" = ?, sleeping = ?) " +
                    "where id = ?"
            db?.execSQL(updateStatementString, arrayOf(time?.time?.div(1000), sleeping, id))
        }
    }
}
