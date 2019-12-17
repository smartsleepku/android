package dk.ku.sund.smartsleep

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import dk.ku.sund.smartsleep.manager.*
import kotlinx.android.synthetic.main.content_attendee_number.*

class AttendeeNumberActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendee_number)
        if (!isInitialized) {
            initializeStore(applicationContext)
            configure()
            trustKU()
            initializeDatabase(this)
            isInitialized = true
        }

        errorcard.alpha = 0.0F

        input.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val code = input.text.toString()
                run {
                    if (validAttendee(code)) {
                        runOnUiThread {
                            volatileStore["attendeeCode"] = code
                            val intent = Intent(this, EmailActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            errorcard.alpha = 1.0F
                        }
                    }
                }
                return@setOnKeyListener true
            }
            false
        }
    }

}
