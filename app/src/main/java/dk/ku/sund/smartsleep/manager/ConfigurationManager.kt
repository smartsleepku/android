package dk.ku.sund.smartsleep.manager

import com.google.gson.GsonBuilder
import dk.ku.sund.smartsleep.model.Configuration
import java.util.*

private fun dateWithHour(hour: Int): Date {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, 0)
    return cal.time
}

val gson = GsonBuilder()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    .create()

val defaultConfiguration = Configuration(
    dateWithHour(7),
    dateWithHour(22),
    dateWithHour(10),
    dateWithHour(23)
)

var configuration: Configuration?
    get() = load("configuration", Configuration::class.java)
    set(value) {
        value ?: return
        store("configuration", value)
    }

val currentConfiguration: Configuration
    get() = configuration ?: defaultConfiguration

val hasConfiguration: Boolean
    get() = configuration != null
