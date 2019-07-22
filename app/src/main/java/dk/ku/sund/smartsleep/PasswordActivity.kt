package dk.ku.sund.smartsleep

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import dk.ku.sund.smartsleep.manager.volatileStore
import kotlinx.android.synthetic.main.content_password.*

class PasswordActivity : Activity() {

    fun validPassword(target: CharSequence): Boolean {
        return target.length > 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)

        errorcard.alpha = 0.0F

        input.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val password = input.text.toString()
                if (validPassword(password)) {
                    volatileStore["password"] = password
                    val intent = Intent(this, RequestAccessActivity::class.java)
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
