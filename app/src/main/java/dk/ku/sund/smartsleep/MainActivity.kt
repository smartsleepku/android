package dk.ku.sund.smartsleep

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import dk.ku.sund.smartsleep.manager.*
import dk.ku.sund.smartsleep.service.ActivityRecognitionService
import dk.ku.sund.smartsleep.service.ScreenService
import dk.ku.sund.smartsleep.service.HeartbeatService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initializeStore(applicationContext)
        initializeDatabase(this)

        startService(Intent(this@MainActivity, ScreenService::class.java))
        startService(Intent(this@MainActivity, ActivityRecognitionService::class.java))
        startService(Intent(this@MainActivity, HeartbeatService::class.java))

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
