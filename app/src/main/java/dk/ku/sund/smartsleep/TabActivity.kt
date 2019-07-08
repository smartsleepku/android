package dk.ku.sund.smartsleep

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import dk.ku.sund.smartsleep.manager.*
import dk.ku.sund.smartsleep.model.Night
import kotlinx.android.synthetic.main.activity_tab.*
import kotlinx.android.synthetic.main.content_tab.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class TabActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab)
        configure()
        trustKU()

        fab.setOnClickListener {
            val intent = Intent(this, ConfigureActivity::class.java)
            startActivity(intent)
        }

        navigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.questionnaire -> {
                    val intent = Intent(this, SurveyActivity::class.java)
                    startActivity(intent)
                }
                R.id.calendar -> {
                    val intent = Intent(this, HistoryActivity::class.java)
                    startActivity(intent)
                }
                R.id.advice -> {
                    val openURL = Intent(Intent.ACTION_VIEW)
                    openURL.data = Uri.parse("https://www.smartsleep.ku.dk/gode-raad-om-soevn/")
                    startActivity(openURL)
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()

        if (hasConfiguration) {
            GlobalScope.launch {
                generateNights()
                var night = fetchOneNight(Date())
                if (night == null) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DATE, -1)
                    night = fetchOneNight(cal.time)
                }
                night ?: return@launch
                runOnUiThread {
                    disruptions.text = getString(R.string.tonight_disruptions_suffix, night.disruptionCount)
                    longestRest.text = getString(R.string.tonight_longest_rest_suffix,
                        night.longestSleepDuration?.div(3600)?.toInt(),
                        (night.longestSleepDuration?.div(60))?.rem(60)?.toInt())
                    unrest.text = getString(R.string.tonight_unrest_suffix,
                        night.unrestDuration?.div(3600)?.toInt(),
                        (night.unrestDuration?.div(60))?.rem(60)?.toInt())
                }
            }
        } else {
            val intent = Intent(this, ConfigureActivity::class.java)
            startActivity(intent)
        }
    }
}