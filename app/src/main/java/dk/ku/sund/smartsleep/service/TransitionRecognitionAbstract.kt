package dk.ku.sund.smartsleep.service

import android.content.Context

abstract class TransitionRecognitionAbstract {
    abstract fun startTracking(context: Context)
    abstract fun stopTracking()
}
