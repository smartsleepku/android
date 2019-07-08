package dk.ku.sund.smartsleep.manager

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dk.ku.sund.smartsleep.model.Night
import dk.ku.sund.smartsleep.model.RecognizedActivity
import dk.ku.sund.smartsleep.model.Rest
import dk.ku.sund.smartsleep.model.Sleep

private val DATABASE_VERSION = 1
private val DATABASE_NAME = "smartsleep.db"

var databaseHandler: DatabaseHandler? = null
var db: SQLiteDatabase? = null

class DatabaseHandler(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        Sleep.initializeDatabase(db)
        Rest.initializeDatabase(db)
        Night.initializeDatabase(db)
        RecognizedActivity.initializeDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int,
                           newVersion: Int) {

    }
}

fun initializeDatabase(context: Context) {
    databaseHandler = DatabaseHandler(context)
    db = databaseHandler?.writableDatabase
}
