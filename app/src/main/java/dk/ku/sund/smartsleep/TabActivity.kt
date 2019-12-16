package dk.ku.sund.smartsleep

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import dk.ku.sund.smartsleep.manager.*
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
        initializeStore(applicationContext)

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

        if (!hasConfiguration) {
            val intent = Intent(this, ConfigureActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) return
        update()
    }

    private fun update() {
        Log.i("TabActivity", "update, hasConfiguration: ${hasConfiguration}")
        if (hasConfiguration) {
            GlobalScope.launch {
                generateNights(object : NightGeneratorUpdateHolder() {
                    override fun update(db: SQLiteDatabase?) {
                        val done = this.done
                        val total = this.total
                        val current = this.current
                        Log.i("DatabaseMutex", "NightManager-UpdateSubroutineTab: before UI thread")
                        runOnUiThread {
                            if (done) {
                                loading.visibility = View.INVISIBLE
                            } else {
                                loading.visibility = View.VISIBLE
                                loading.max = total.toInt()
                                loading.progress = current.toInt()
                            }
                        }
                        Log.i("DatabaseMutex", "NightManager-UpdateSubroutineTab: after UI thread")
                    }
                })
                var night = fetchOneNight(Date())
                if (night == null || (night.longestSleepDuration!! + night.unrestDuration!! == 0L)) {
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
        }
    }
}