package dk.ku.sund.smartsleep.model

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import dk.ku.sund.smartsleep.manager.acquireDatabase
import dk.ku.sund.smartsleep.manager.dbMutex
import dk.ku.sund.smartsleep.manager.releaseDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import java.util.*

data class RecognizedActivity (
    var id: Int?,
    var type: String?,
    var confidence: Int?,
    var time: Date?
) {
    companion object {
        fun initializeDatabase(db: SQLiteDatabase) {
            val createTableString = "create table if not exists activities(" +
                    "id integer primary key autoincrement," +
                    "\"type\" varchar(12) not null," +
                    "confidence integer not null," +
                    "\"time\" integer not null unique)"
            db.execSQL(createTableString)
        }
    }

    constructor(cursor: Cursor): this(null, null, null, null) {
        id = cursor.getInt(cursor.getColumnIndex("id"))
        type = cursor.getString(cursor.getColumnIndex("type"))
        confidence = cursor.getInt(cursor.getColumnIndex("confidence"))
        time = Date(cursor.getLong(cursor.getColumnIndex("time")))
    }

    fun save() = runBlocking {
        Log.i("DatabaseMutex", "RecognizedActivity: mutex = $dbMutex")
        dbMutex.withLock {
            val db = acquireDatabase()
            try {
                if (id == null) {
                    val insertStatementString = "insert or replace into activities (\"type\", confidence, \"time\") " +
                            "values (?, ?, ?)"
                    db?.execSQL(insertStatementString, arrayOf(type, confidence, (time ?: Date()).time))
                } else {
                    val updateStatementString = "update activities set (\"type\" = ?, confidence = ?, \"time\" = ?) " +
                            "where id = ?"
                    db?.execSQL(updateStatementString, arrayOf(type, confidence, (time ?: Date()).time, id))
                }
            } finally {
                releaseDatabase()
            }
        }
    }
}
