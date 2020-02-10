package dk.ku.sund.smartsleep.manager

import android.os.Build
import android.util.Base64
import android.util.Log
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import dk.ku.sund.smartsleep.model.*
import kotlinx.coroutines.runBlocking
import java.util.*

data class AttendeeResult(var code: String = "", var valid: Boolean = false)

fun validAttendee(code: String): Boolean = runBlocking {
    val result = "/auth/attendee/${code}"
        .httpGet()
        .header(mapOf(
            "x-client-id" to "android",
            "x-client-secret" to CLIENT_SECRET
        ))
        .awaitObject(gsonDeserializerOf(AttendeeResult::class.java))
    result.valid
}

suspend fun postCredentials() {
    try {
        val body = AuthLoginBody(
            volatileStore["email"] as String,
            volatileStore["password"] as String,
            "android",
            CLIENT_SECRET,
            volatileStore["attendeeCode"] as String
        )
        val token = "/auth/attendee".httpPost()
            .header(mapOf(
                "Content-Type" to "application/json"
            ))
            .jsonBody(body)
            .awaitObject(gsonDeserializerOf(AuthLoginResponseToken::class.java))
        store("jwt", token.jwt)
    } catch (e: Exception) {
        Log.e("AuthenticationManager", e.stackTrace.joinToString("\n"))
    }
}

suspend fun postDebugInfo() {
    val debugInfo = DebugInfo(
        Date(),
        Build.MODEL,
        Build.MANUFACTURER,
        Build.VERSION.RELEASE,
        "android"
    )
    "/debug".httpPost()
        .header(mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${jwt}"
        ))
        .body(gson.toJson(debugInfo))
        .awaitStringResponse()
}

suspend fun postAttendee() {
    "/attendee".httpPut()
        .header(mapOf(
            "Content-Type" to "application/json",
            "Authorization" to "Bearer ${jwt}"
        ))
        .body(gson.toJson(Attendee(
            TimeZone.getDefault().rawOffset / 1000,
            currentConfiguration.weekdayMorning,
            currentConfiguration.weekdayEvening,
            currentConfiguration.weekendMorning,
            currentConfiguration.weekendEvening
        )))
        .awaitStringResponse()
}

var jwt: String
    get() = load("jwt", String::class.java) ?: ""
    set(value) = store("jwt", value)

val hasJwt: Boolean
    get() = jwt != ""

val userId: String
    get() = gson
        .fromJson(
            Base64
                .decode(jwt.split(".")[1], Base64.DEFAULT)
                .toString(charset("UTF-8")),
            Token::class.java)
        .userId
