package dk.ku.sund.smartsleep.service

import android.R
import android.app.Notification
import androidx.core.app.NotificationCompat
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.IBinder
import android.util.Log
import dk.ku.sund.smartsleep.manager.bulkPostHeartbeat
import dk.ku.sund.smartsleep.manager.configure
import dk.ku.sund.smartsleep.manager.trustKU
import dk.ku.sund.smartsleep.model.Heartbeat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

const val NOTIFICATION_CHANNEL_ID = "cid"

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

        var builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            //.setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Smart Sleep")
            .setContentText("Smart Sleep is currently running")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        startForeground(1, builder.build())

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        receiver = ScreenReceiver()
        registerReceiver(receiver, filter)

        if (timer == null) {
            timer = Timer()
        }
        Log.d("SHeartbeat", "Screen created")
        timer!!.scheduleAtFixedRate(HeartbeatTimerTask(), 0, HEARTBEAT_INTERVAL)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        timer = null
        Log.d("SHeartbeat", "Screen destroyed")
        super.onDestroy()
    }

    internal inner class HeartbeatTimerTask : TimerTask() {

        override fun run() {
            handler.post {
                Log.d("SHeartbeat", "Heartbeat generated")
                val heartbeat = Heartbeat(null, Date())
                heartbeat.save()
                GlobalScope.launch {
                    bulkPostHeartbeat()
                }
            }
        }

    }
}