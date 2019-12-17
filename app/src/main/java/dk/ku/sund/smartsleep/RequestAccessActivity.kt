package dk.ku.sund.smartsleep

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import dk.ku.sund.smartsleep.manager.*
import kotlinx.android.synthetic.main.content_request_access.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RequestAccessActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_access)
        if (!isInitialized) {
            initializeStore(applicationContext)
            configure()
            trustKU()
            initializeDatabase(this)
            isInitialized = true
        }

        button.setOnClickListener {
            GlobalScope.launch {
                postCredentials()
            }
            val intent = Intent(this, TabActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
