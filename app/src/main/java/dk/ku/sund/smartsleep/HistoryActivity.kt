package dk.ku.sund.smartsleep

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import dk.ku.sund.smartsleep.manager.fetchNights
import dk.ku.sund.smartsleep.model.Night
import kotlinx.android.synthetic.main.activity_history.*
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
        val nights = fetchNights()
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
    }
}

