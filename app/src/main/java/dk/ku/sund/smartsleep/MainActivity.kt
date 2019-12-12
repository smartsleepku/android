package dk.ku.sund.smartsleep

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import dk.ku.sund.smartsleep.manager.*
import dk.ku.sund.smartsleep.service.ActivityRecognitionService
import dk.ku.sund.smartsleep.service.NOTIFICATION_CHANNEL_ID
import dk.ku.sund.smartsleep.service.ScreenService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initializeStore(applicationContext)
        initializeDatabase(this)

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification-Channel"
            val descriptionText = "Smart Sleep notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        startService(Intent(this@MainActivity, ScreenService::class.java))
        startService(Intent(this@MainActivity, ActivityRecognitionService::class.java))
        Log.d("SHeartbeat", "App is opened")

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
