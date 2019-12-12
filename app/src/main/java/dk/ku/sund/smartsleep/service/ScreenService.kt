package dk.ku.sund.smartsleep.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import dk.ku.sund.smartsleep.manager.bulkPostHeartbeat
import dk.ku.sund.smartsleep.manager.configure
import dk.ku.sund.smartsleep.manager.trustKU
import dk.ku.sund.smartsleep.model.Heartbeat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


const val NOTIFICATION_CHANNEL_ID = "cid"
const val HEARTBEAT_INTERVAL = (5 * 60 * 1000).toLong() // 10 minutes

class ScreenService : Service() {

    var receiver: ScreenReceiver? = null
    private var alarmMgr: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    //val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(2)

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

        //val heartbeatTask = HeartbeatTimerTask("Heartbeat task")
        //executor.scheduleAtFixedRate(heartbeatTask, 0, HEARTBEAT_INTERVAL, TimeUnit.SECONDS)

        alarmMgr = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(this, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, 0)
        }
//        alarmMgr!!.setInexactRepeating(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime(),
//            HEARTBEAT_INTERVAL,
//            alarmIntent
//        )
        alarmMgr!!.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            alarmIntent
        )

        Log.d("SHeartbeat", "Screen created")
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        Log.d("SHeartbeat", "Screen destroyed")
        super.onDestroy()
    }

    internal inner class HeartbeatTimerTask(val name: String) : Runnable {

        override fun run() {
            Log.d("SHeartbeat", "Heartbeat generated")
            val heartbeat = Heartbeat(null, Date())
            heartbeat.save()
            GlobalScope.launch {
                bulkPostHeartbeat()
            }
        }

    }
}