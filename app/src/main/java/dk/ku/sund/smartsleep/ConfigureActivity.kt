package dk.ku.sund.smartsleep

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import dk.ku.sund.smartsleep.manager.configuration
import dk.ku.sund.smartsleep.manager.currentConfiguration
import dk.ku.sund.smartsleep.manager.defaultConfiguration
import dk.ku.sund.smartsleep.manager.hasConfiguration
import kotlinx.android.synthetic.main.activity_configure.*
import kotlinx.android.synthetic.main.content_configure.*
import android.app.TimePickerDialog
import java.util.*


class ConfigureActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure)

        fab.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        ok.setOnClickListener {
            finish()
        }

        weekday_evening.setOnClickListener {
            val config = currentConfiguration
            val cal = Calendar.getInstance()
            cal.time = config.weekdayEvening
            TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener(function = { view, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    config.weekdayEvening = cal.time
                    weekday_evening_time.text = DateFormat.format("HH:mm", cal.time)
                    configuration = config
                }),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true)
                .show()
        }

        weekday_morning.setOnClickListener {
            val config = currentConfiguration
            val cal = Calendar.getInstance()
            cal.time = config.weekdayMorning
            TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener(function = { view, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    config.weekdayMorning = cal.time
                    weekday_morning_time.text = DateFormat.format("HH:mm", cal.time)
                    configuration = config
                }),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true)
                .show()
        }

        weekend_evening.setOnClickListener {
            val config = currentConfiguration
            val cal = Calendar.getInstance()
            cal.time = config.weekendEvening
            TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener(function = { view, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    config.weekendEvening = cal.time
                    weekend_evening_time.text = DateFormat.format("HH:mm", cal.time)
                    configuration = config
                }),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true)
                .show()
        }

        weekend_morning.setOnClickListener {
            val config = currentConfiguration
            val cal = Calendar.getInstance()
            cal.time = config.weekendMorning
            TimePickerDialog(this,
                TimePickerDialog.OnTimeSetListener(function = { view, hour, minute ->
                    cal.set(Calendar.HOUR_OF_DAY, hour)
                    cal.set(Calendar.MINUTE, minute)
                    config.weekendMorning = cal.time
                    weekend_morning_time.text = DateFormat.format("HH:mm", cal.time)
                    configuration = config
                }),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false)
                .show()
        }

    }

    override fun onResume() {
        super.onResume()
        val config = currentConfiguration

        weekday_evening_time.text = DateFormat.format("HH:mm", config.weekdayEvening)
        weekday_morning_time.text = DateFormat.format("HH:mm", config.weekdayMorning)
        weekend_evening_time.text = DateFormat.format("HH:mm", config.weekendEvening)
        weekend_morning_time.text = DateFormat.format("HH:mm", config.weekendMorning)
    }

    override fun onPause() {
        super.onPause()

        if (!hasConfiguration) configuration = defaultConfiguration
    }
}
