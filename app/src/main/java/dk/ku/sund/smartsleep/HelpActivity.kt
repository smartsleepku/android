package dk.ku.sund.smartsleep

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_help.*


class HelpActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)
        web.loadUrl("file:///android_asset/help.html")
    }
}
