package dk.ku.sund.smartsleep.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import java.util.Timer
import java.util.TimerTask
import android.os.IBinder
import android.util.Log
import androidx.work.WorkManager
import dk.ku.sund.smartsleep.manager.bulkPostHeartbeat
import dk.ku.sund.smartsleep.manager.configure
import dk.ku.sund.smartsleep.manager.trustKU
import dk.ku.sund.smartsleep.manager.uploadRequest
import dk.ku.sund.smartsleep.model.Heartbeat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

private const val HEARTBEAT_INTERVAL = (10 * 1000).toLong() // 10 seconds

class HeartbeatService : Service() {

    private val handler = Handler()
    private var timer: Timer? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        configure()
        trustKU()
        if (timer == null) {
            timer = Timer()
        }
        timer!!.scheduleAtFixedRate(HeartbeatTimerTask(), HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL)
        Log.i("HeartbeatService", "Started heartbeat")
        WorkManager.getInstance(this).enqueue(uploadRequest)
        Log.i("HeartbeatService", "Started heartbeat uploader")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, startId, startId)
        return START_STICKY
    }

    internal inner class HeartbeatTimerTask : TimerTask() {

        override fun run() {
            handler.post {
                val heartbeat = Heartbeat(null, Date())
                Log.i("HeartbeatService", "Heartbeat event")
                heartbeat.save()
            }
        }

    }
}
