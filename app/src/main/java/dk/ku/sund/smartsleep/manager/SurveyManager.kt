package dk.ku.sund.smartsleep.manager

import android.util.Log
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpPost
import dk.ku.sund.smartsleep.model.SessionKeyResult
import dk.ku.sund.smartsleep.model.Survey
import dk.ku.sund.smartsleep.model.SurveyRequest
import dk.ku.sund.smartsleep.model.SurveyResult
import kotlinx.coroutines.runBlocking

val sessionKey: String?
    get() = runBlocking {
        if (!hasJwt) return@runBlocking null
        val response = "/index.php/admin/remotecontrol/".httpPost()
            .header(mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer ${jwt}"
            ))
            .jsonBody(SurveyRequest(
                "1",
                "get_session_key",
                arrayOf("admin", ADMIN_CREDENTIALS)
            ))
            .responseObject<SessionKeyResult>()
        if (response.third.component2() != null) {
            Log.e("SurveyManager", "Failed getting session key: ${response.third.component2()}")
        }
        return@runBlocking response.third.component1()?.result
    }

val surveys: Array<Survey>
    get() = runBlocking {
        val sessionKey = sessionKey ?: return@runBlocking emptyArray<Survey>()
        val response = "/index.php/admin/remotecontrol/".httpPost()
            .header(mapOf(
                "Content-Type" to "application/json",
                "Authorization" to "Bearer ${jwt}"
            ))
            .jsonBody(SurveyRequest(
                "1",
                "list_surveys",
                arrayOf(sessionKey, "admin")
            ))
            .responseObject<SurveyResult>()
        if (response.third.component2() != null) {
            Log.e("SurveyManager", "Failed getting surveys: ${response.third.component2()}")
        }
        return@runBlocking response.third.component1()?.result ?: emptyArray<Survey>()
    }
