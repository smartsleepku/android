package dk.ku.sund.smartsleep.model

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import dk.ku.sund.smartsleep.manager.db
import java.util.*

data class Night(
    var id: Int?,
    var from: Date?,
    var to: Date?,
    var disruptionCount: Int?,
    var longestSleepDuration: Long?,
    var unrestDuration: Long?
) {
    companion object {
        fun initializeDatabase(db: SQLiteDatabase) {
            val createTableString = "create table if not exists nights(" +
                    "id integer primary key autoincrement," +
                    "\"from\" integer not null unique," +
                    "\"to\" integer not null," +
                    "disruptionCount integer not null," +
                    "longestSleepDuration integer not null," +
                    "unrestDuration integer not null)"
            db.execSQL(createTableString)
        }
    }

    constructor(cursor: Cursor): this(null, null, null, null, null, null) {
        id = cursor.getInt(cursor.getColumnIndex("id"))
        from = Date(cursor.getLong(cursor.getColumnIndex("from")))
        to = Date(cursor.getLong(cursor.getColumnIndex("to")))
        disruptionCount = cursor.getInt(cursor.getColumnIndex("disruptionCount"))
        longestSleepDuration = cursor.getLong(cursor.getColumnIndex("longestSleepDuration"))
        unrestDuration = cursor.getLong(cursor.getColumnIndex("unrestDuration"))
    }

    fun save() {
        if (id == null) {
            val insertStatementString = "insert or replace into nights (\"from\", \"to\", disruptionCount, " +
                    "longestSleepDuration, unrestDuration) " +
                    "values (?, ?, ?, ?, ?)"
            db?.execSQL(insertStatementString, arrayOf(from, to, disruptionCount, longestSleepDuration, unrestDuration))
        } else {
            val updateStatementString = "update nights set (\"from\" = ?, \"to\" = ?, disruptionCount = ?, " +
                    "longestSleepDuration = ?, unrestDuration = ?) " +
                    "where id = ?"
            db?.execSQL(updateStatementString, arrayOf(from, to, disruptionCount, longestSleepDuration, unrestDuration, id))
        }
    }

}
