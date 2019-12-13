package dk.ku.sund.smartsleep.service

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import dk.ku.sund.smartsleep.manager.*
import dk.ku.sund.smartsleep.model.Heartbeat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmMgr = context.getSystemService(Service.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { alIntent ->
            PendingIntent.getBroadcast(context, 0, alIntent, 0)
        }
        alarmMgr.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime()+HEARTBEAT_INTERVAL,
            alarmIntent
        )

        val manager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if (service.service.className == "dk.ku.sund.smartsleep.service.ScreenService") {
                initializeStore(context)
                configure()
                trustKU()
                initializeDatabase(context)
                val heartbeat = Heartbeat(null, Date())
                heartbeat.save()
                bulkPostHeartbeat()
                deinitializeDatabase()
            }
        }

        context.startService(Intent(context, ScreenService::class.java))
        context.startService(Intent(context, ActivityRecognitionService::class.java))
    }
}
