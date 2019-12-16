package dk.ku.sund.smartsleep.manager

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import dk.ku.sund.smartsleep.model.*
import kotlinx.coroutines.sync.Mutex
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.nio.channels.FileLock

private val DATABASE_VERSION = 2
private val DATABASE_NAME = "smartsleep.db"

var databaseHandler: DatabaseHandler? = null
private var db: SQLiteDatabase? = null
private var dbPath: String? = null

var dbMutex = Mutex(false)
private var dbLock: FileLock? = null

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
    dbPath = context.getDatabasePath(DATABASE_NAME).path
    Log.i("DatabaseHandler", dbPath)
    Log.i("DatabaseHandler", "init: mutex = $dbMutex")
}

fun acquireDatabase(): SQLiteDatabase? {
    //Log.i("DatabaseHandler", "Handler = ${databaseHandler.toString()}")
    val fis = FileOutputStream("$dbPath.lock")
    val fileChannel: FileChannel = fis.channel
    dbLock = fileChannel.tryLock(0L, Long.MAX_VALUE, false)
    db = try {
        databaseHandler?.writableDatabase
    } catch (e: SQLiteException) {
        Log.e("DatabaseHandler", e.stackTrace.joinToString("\n"))
        store("dberror", e.stackTrace.joinToString("\n"))
        null
    }
    //Log.i("DatabaseHandler", "acquire: mutex = ${dbMutex}")
    return db
}

fun releaseDatabase() {
    databaseHandler?.close()
    dbLock?.close()
}
