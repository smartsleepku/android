package dk.ku.sund.smartsleep

import android.app.Activity
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dk.ku.sund.smartsleep.manager.*
import dk.ku.sund.smartsleep.model.Night
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : Activity() {

    private data class ViewHolder(
        var title: TextView,
        var disruptionCount: TextView,
        var sleepDuration: TextView,
        var unrestDuration: TextView
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val inflater = LayoutInflater.from(this)
        lateinit var nights: List<Night>
        runBlocking {
            Log.i("DatabaseMutex", "HistoryActivity: mutex = $dbMutex")
            dbMutex.withLock {
                val db = acquireDatabase()
                try {
                    nights = fetchNights(db)
                } finally {
                    releaseDatabase()
                }
            }
        }
        val locale = Locale("da_DK")
        val dateFormatter = SimpleDateFormat("d. MMMM", locale)
        val timeFormatter = SimpleDateFormat("HH:mm", locale)

        list.adapter = object : ArrayAdapter<String>(this, list.id) {

            override fun getCount(): Int {
                return nights.count()
            }
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val holder: ViewHolder
                var line = convertView
                if (line == null) {
                    line = inflater.inflate(R.layout.line_night, null)
                    holder = ViewHolder(
                        line.findViewById(R.id.line_title),
                        line.findViewById(R.id.line_disruption_count),
                        line.findViewById(R.id.line_sleep_duration),
                        line.findViewById(R.id.line_unrest_duration)
                    )
                    line.setTag(holder)
                } else {
                    holder = line.tag as ViewHolder
                }

                val night = nights[position]
                holder.title.text = getString(R.string.line_title_value,
                    dateFormatter.format(night.from),
                    timeFormatter.format(night.from),
                    timeFormatter.format(night.to)
                )
                holder.disruptionCount.text = "${night.disruptionCount}"
                val hours = (night.longestSleepDuration ?: 0) / 3600
                val minutes = ((night.longestSleepDuration ?: 0) % 3600) / 60
                holder.sleepDuration.text = getString(R.string.line_sleep_duration_value,
                    hours,
                    minutes
                )
                holder.unrestDuration.text = "${(night.unrestDuration ?: 0) / 60}"

                return line!!
            }
        }

        GlobalScope.launch {
            generateNights(object : NightGeneratorUpdateHolder() {
                override fun update(db: SQLiteDatabase?) {
                    Log.i("DatabaseMutex", "NightManager-UpdateSubroutineHistory: mutex = $dbMutex")
                    val fetched = fetchNights(db)
                    Log.i("DatabaseMutex", "NightManager-UpdateSubroutineHistory: fetched nights")
                    val done = this.done
                    runOnUiThread {
                        if (done) { loading.visibility = View.GONE }
                        else { loading.visibility = View.VISIBLE }
                        nights = fetched
                        (list.adapter as ArrayAdapter<String>).notifyDataSetChanged()
                    }
                    Log.i("DatabaseMutex", "NightManager-UpdateSubroutineHistory: done UI stuff")
                }
            })
        }

    }
}

