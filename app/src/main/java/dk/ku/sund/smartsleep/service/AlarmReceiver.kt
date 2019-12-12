package dk.ku.sund.smartsleep.service

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import dk.ku.sund.smartsleep.manager.*
import dk.ku.sund.smartsleep.model.Heartbeat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    private var isInitialized = false

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SHeartbeat", "Alarm event")
        var alarmMgr: AlarmManager? = null
        lateinit var alarmIntent: PendingIntent
        val manager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (service.service.className == "dk.ku.sund.smartsleep.service.ScreenService") {
                //Log.d("SHeartbeat", "SmartSleep is running")
                if (!isInitialized) {
                    Log.d("SHeartbeat", "Initializing second process environment")
                    configure()
                    trustKU()
                    initializeStore(context)
                    initializeDatabase(context)
                    isInitialized = true
                }
                Log.d("SHeartbeat", "Heartbeat generated")
                val heartbeat = Heartbeat(null, Date())
                heartbeat.save()
                GlobalScope.launch {
                    bulkPostHeartbeat()
                }
                alarmMgr = context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
                alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                    PendingIntent.getBroadcast(context, 0, intent, 0)
                }
                alarmMgr!!.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime()+HEARTBEAT_INTERVAL,
                    alarmIntent
                )
            }
        }
    }
}
