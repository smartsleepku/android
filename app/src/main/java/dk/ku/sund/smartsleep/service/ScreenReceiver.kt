package dk.ku.sund.smartsleep.service

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import dk.ku.sund.smartsleep.isInitialized
import dk.ku.sund.smartsleep.manager.*
import dk.ku.sund.smartsleep.model.Sleep
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class ScreenReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!isInitialized) {
            initializeStore(context)
            configure()
            trustKU()
            initializeDatabase(context)
            isInitialized = true
        }

        val sleep: Sleep
        Log.i("ScreenReceiver", "got screen event")
        if (intent.action == Intent.ACTION_SCREEN_OFF) {
            sleep = Sleep(null, Date(), true)
            sleep.save()
        } else if (intent.action == Intent.ACTION_SCREEN_ON) {
            sleep = Sleep(null, Date(), false)
            sleep.save()
        } else {
            Log.w("ScreenReceiver", "unknown screen event")
            return
        }
        Log.i("ScreenReceiver", "sleep is saved")
        updateLatestRest(sleep = sleep)
        Log.i("ScreenReceiver", "rest is updated")

        GlobalScope.launch {
            bulkPostSleep()
            Log.i("ScreenReceiver", "sleep is posted")
        }

        val i = Intent(context, ScreenService::class.java)
        context.startService(i)
    }
}
