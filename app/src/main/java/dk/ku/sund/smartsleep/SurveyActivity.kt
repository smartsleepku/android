package dk.ku.sund.smartsleep

import android.app.Activity
import android.os.Bundle
import com.github.kittinunf.fuel.core.FuelManager
import android.webkit.WebView
import dk.ku.sund.smartsleep.manager.*
import kotlinx.android.synthetic.main.activity_survey.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SurveyActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)

        web.settings.javaScriptEnabled = true

        if (!hasJwt) return

        GlobalScope.launch {
            val survey = surveys.filter { it.active == "Y" }
                .firstOrNull()
            survey ?: return@launch
            runOnUiThread {

                web.webViewClient = object : TrustAllClient() {
                    override fun onPageFinished(view: WebView, url: String) {

                        web.webViewClient = TrustAllClient()
                        web.loadUrl(FuelManager.instance.basePath
                                + "/index.php/"
                                + survey.sid
                                + "?lang=da&token=" + userId)
                    }
                }

                web.loadUrl(FuelManager.instance.basePath + "/auth/setcookie?auth=" + jwt)
            }
        }

    }
}
