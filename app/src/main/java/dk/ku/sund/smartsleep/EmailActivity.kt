package dk.ku.sund.smartsleep

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.util.Patterns
import android.text.TextUtils
import dk.ku.sund.smartsleep.manager.volatileStore
import kotlinx.android.synthetic.main.content_email.*

class EmailActivity : Activity() {

    fun validEmail(target: CharSequence): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email)

        errorcard.alpha = 0.0F

        input.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val email = input.text.toString()
                if (validEmail(email)) {
                    volatileStore["email"] = email
                    val intent = Intent(this, PasswordActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    errorcard.alpha = 1.0F
                }
                return@setOnKeyListener true
            }
            false
        }
    }

}
