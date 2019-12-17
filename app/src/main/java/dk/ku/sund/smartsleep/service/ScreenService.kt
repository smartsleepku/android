package dk.ku.sund.smartsleep.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dk.ku.sund.smartsleep.isInitialized
import dk.ku.sund.smartsleep.manager.configure
import dk.ku.sund.smartsleep.manager.initializeDatabase
import dk.ku.sund.smartsleep.manager.initializeStore
import dk.ku.sund.smartsleep.manager.trustKU


const val NOTIFICATION_CHANNEL_ID = "cid"

class ScreenService : Service() {

    var receiver: ScreenReceiver? = null

    inner class Binder : android.os.Binder() {
        fun getService(): ScreenService = this@ScreenService
    }

    private val binder = Binder()
    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        if (!isInitialized) {
            initializeStore(applicationContext)
            configure()
            trustKU()
            initializeDatabase(this)
            isInitialized = true
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Smart Sleep")
            .setContentText("Smart Sleep is currently running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        startForeground(1, builder.build())

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        receiver = ScreenReceiver()
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }
}