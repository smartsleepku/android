package dk.ku.sund.smartsleep

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle

import kotlinx.android.synthetic.main.activity_welcome.*
import kotlinx.android.synthetic.main.content_welcome.*

class WelcomeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        fab.setOnClickListener {
            val intent = Intent(this, AttendeeNumberActivity::class.java)
            startActivity(intent)
            finish()
        }

        prerequisite.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://smartsleep.ku.dk")
            startActivity(openURL)
        }
    }

}
