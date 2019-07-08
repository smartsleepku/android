package dk.ku.sund.smartsleep.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.content.IntentFilter
import dk.ku.sund.smartsleep.manager.configure
import dk.ku.sund.smartsleep.manager.trustKU

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
        configure()
        trustKU()
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