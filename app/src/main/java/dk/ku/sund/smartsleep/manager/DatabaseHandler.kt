package dk.ku.sund.smartsleep.manager

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dk.ku.sund.smartsleep.model.*

private val DATABASE_VERSION = 2
private val DATABASE_NAME = "smartsleep.db"

var databaseHandler: DatabaseHandler? = null
var db: SQLiteDatabase? = null

class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        Sleep.initializeDatabase(db)
        Rest.initializeDatabase(db)
        Night.initializeDatabase(db)
        RecognizedActivity.initializeDatabase(db)
        Heartbeat.initializeDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int,
                           newVersion: Int) {
        if (oldVersion == 1 && newVersion == 2) {
            Heartbeat.initializeDatabase(db)
        }
    }
}

fun initializeDatabase(context: Context) {
    databaseHandler = DatabaseHandler(context)
    db = databaseHandler?.writableDatabase
}

fun deinitializeDatabase() {
    db?.close()
}
