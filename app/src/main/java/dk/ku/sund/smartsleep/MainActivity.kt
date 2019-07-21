package dk.ku.sund.smartsleep

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import dk.ku.sund.smartsleep.manager.hasJwt
import dk.ku.sund.smartsleep.manager.initializeConfiguration
import dk.ku.sund.smartsleep.manager.initializeDatabase
import dk.ku.sund.smartsleep.service.ActivityRecognitionService
import dk.ku.sund.smartsleep.service.ScreenService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initializeConfiguration(applicationContext)

        initializeDatabase(this)

        startService(Intent(this@MainActivity, ScreenService::class.java))
        startService(Intent(this@MainActivity, ActivityRecognitionService::class.java))

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
