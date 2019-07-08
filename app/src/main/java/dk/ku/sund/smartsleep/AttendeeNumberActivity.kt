package dk.ku.sund.smartsleep

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.content.edit
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import dk.ku.sund.smartsleep.manager.configure
import dk.ku.sund.smartsleep.manager.trustKU
import dk.ku.sund.smartsleep.manager.validAttendee
import kotlinx.android.synthetic.main.content_attendee_number.*

class AttendeeNumberActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendee_number)
        configure()
        trustKU()
        val prefs = SecuredPreferenceStore.getSharedInstance()

        errorcard.alpha = 0.0F

        input.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val code = input.text.toString()
                run {
                    if (validAttendee(code)) {
                        runOnUiThread {
                            prefs.edit {
                                putString("attendeeCode", code)
                            }
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
