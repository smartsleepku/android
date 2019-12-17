package dk.ku.sund.smartsleep

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import dk.ku.sund.smartsleep.manager.*
import dk.ku.sund.smartsleep.service.ActivityRecognitionService
import dk.ku.sund.smartsleep.service.AlarmReceiver
import dk.ku.sund.smartsleep.service.NOTIFICATION_CHANNEL_ID
import dk.ku.sund.smartsleep.service.ScreenService

var isInitialized = false

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (!isInitialized) {
            initializeStore(applicationContext)
            configure()
            trustKU()
            initializeDatabase(this)
            isInitialized = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SmartSleep-Channel"
            val descriptionText = "Smart Sleep notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        startService(Intent(this@MainActivity, ScreenService::class.java))
        startService(Intent(this@MainActivity, ActivityRecognitionService::class.java))

        val alarmMgr = getSystemService(Service.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(this, 0, intent, 0)
        }
        alarmMgr.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime(),
            alarmIntent
        )

        if (!hasJwt) {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, TabActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
