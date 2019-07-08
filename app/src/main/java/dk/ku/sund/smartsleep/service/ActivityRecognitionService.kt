package dk.ku.sund.smartsleep.service

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import dk.ku.sund.smartsleep.manager.configure
import dk.ku.sund.smartsleep.manager.postRecognizedActivities
import dk.ku.sund.smartsleep.manager.trustKU
import dk.ku.sund.smartsleep.model.RecognizedActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

val ACTIVITY_REQUEST_CODE = 846028

class ActivityRecognitionService : IntentService("ActivityRecognitionService") {

    private var recognition: TransitionRecognition? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, startId, startId)
        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        configure()
        trustKU()
        recognition = TransitionRecognition()
        recognition?.startTracking(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onHandleIntent(intent: Intent?) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            handleDetectedActivities(result.getProbableActivities())
        }
    }

    private fun handleDetectedActivities(probableActivities: List<DetectedActivity>) {
        for (activity in probableActivities) {
            onDetectedActivity(activity)
        }
    }

    private fun onDetectedActivity(activity: DetectedActivity) {
        val type = when (activity.getType()) {
            DetectedActivity.ON_BICYCLE -> "cycling"
            DetectedActivity.RUNNING -> "running"
            DetectedActivity.WALKING -> "walking"
            DetectedActivity.ON_FOOT -> "walking"
            DetectedActivity.IN_VEHICLE -> "automotive"
            DetectedActivity.STILL -> "stationary"
            else -> "unknown"
        }
        RecognizedActivity(
            null,
            type,
            activity.confidence,
            Date()
        ).save()
        GlobalScope.launch {
            postRecognizedActivities()
        }
    }
}
