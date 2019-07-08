package dk.ku.sund.smartsleep.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition

class TransitionRecognition : TransitionRecognitionAbstract(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    var api: GoogleApiClient? = null
    var intent: PendingIntent? = null
    lateinit var context: Context

    override fun startTracking(context: Context) {
        this.context = context

        if (api == null) {
            api = GoogleApiClient.Builder(context.applicationContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build()
        }

        if (!api!!.isConnected && !api!!.isConnecting) {
            val intent = Intent(this.context, ActivityRecognitionService::class.java)
            this.intent = PendingIntent.getService(this.context, ACTIVITY_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            api!!.connect()
        }
    }

    override fun stopTracking() {
        if (api != null && (api!!.isConnected() || api!!.isConnecting())) {
            api!!.disconnect();
        }
    }

    override fun onConnected(bundle: Bundle?) {
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(api, 60 * 1000, intent)

    }

    override fun onConnectionSuspended(i: Int) {
        stopTracking()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        stopTracking()
    }
}