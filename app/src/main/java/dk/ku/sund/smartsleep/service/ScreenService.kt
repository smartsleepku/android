package dk.ku.sund.smartsleep.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.content.IntentFilter
import android.os.Handler
import android.util.Log
import androidx.work.WorkManager
import dk.ku.sund.smartsleep.manager.configure
import dk.ku.sund.smartsleep.manager.trustKU
import dk.ku.sund.smartsleep.manager.uploadRequest
import dk.ku.sund.smartsleep.model.Heartbeat
import dk.ku.sund.smartsleep.model.Appdebug
import java.util.*

class ScreenService : Service() {

    var receiver: ScreenReceiver? = null

    private val handler = Handler()
    private var timer: Timer? = null
    private val HEARTBEAT_INTERVAL = (1 * 60 * 1000).toLong() // 5 minutes

    inner class Binder : android.os.Binder() {
        fun getService(): ScreenService = this@ScreenService
    }

    private val binder = Binder()
    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("SHeartbeat", "Screen started")
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        configure()
        trustKU()
        val ad = Appdebug(null, Date(), "sstart")
        ad.save()
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        receiver = ScreenReceiver()
        registerReceiver(receiver, filter)

        if (timer == null) {
            timer = Timer()
        }
        Log.d("SHeartbeat", "Screen created")
        timer!!.scheduleAtFixedRate(HeartbeatTimerTask(), 0, HEARTBEAT_INTERVAL)
        WorkManager.getInstance(this).enqueue(uploadRequest)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        timer = null
        Log.d("SHeartbeat", "Screen destroyed")
        val ad = Appdebug(null, Date(), "sdestroy")
        ad.save()
        super.onDestroy()
    }

    internal inner class HeartbeatTimerTask : TimerTask() {

        override fun run() {
            handler.post {
                Log.d("SHeartbeat", "Heartbeat generated")
                val heartbeat = Heartbeat(null, Date())
                heartbeat.save()
            }
        }

    }
}