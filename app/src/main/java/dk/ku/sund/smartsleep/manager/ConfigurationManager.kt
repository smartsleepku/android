package dk.ku.sund.smartsleep.manager

import android.content.Context
import androidx.core.content.edit
import com.google.gson.GsonBuilder
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import dk.ku.sund.smartsleep.model.Configuration
import java.util.*

private fun dateWithHour(hour: Int): Date {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, 0)
    return cal.time
}

private val prefs: SecuredPreferenceStore?
    get() = SecuredPreferenceStore.getSharedInstance()

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
    get() = gson
        .fromJson(prefs!!
            .getString("configuration", null), Configuration::class.java)
    set(value) {
        value ?: return
        prefs?.edit {
            putString("configuration", gson.toJson(value))
        }
    }

val currentConfiguration: Configuration
    get() = configuration ?: defaultConfiguration

val hasConfiguration: Boolean
    get() = configuration != null

private var initialized = false

fun initializeConfiguration(context: Context) {
    if (initialized) return
    initialized = true
    SecuredPreferenceStore.init(context, DefaultRecoveryHandler())
}
