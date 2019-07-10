package dk.ku.sund.smartsleep.manager

import android.util.Base64
import androidx.core.content.edit
import com.github.kittinunf.fuel.coroutines.awaitObject
import com.github.kittinunf.fuel.coroutines.awaitStringResponse
import com.github.kittinunf.fuel.gson.gsonDeserializerOf
import com.github.kittinunf.fuel.gson.jsonBody
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import dk.ku.sund.smartsleep.model.Attendee
import dk.ku.sund.smartsleep.model.AuthLoginBody
import dk.ku.sund.smartsleep.model.AuthLoginResponseToken
import dk.ku.sund.smartsleep.model.Token
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
    val prefs = SecuredPreferenceStore.getSharedInstance()
    val token = "/auth/attendee".httpPost()
        .header(mapOf(
            "Content-Type" to "application/json"
        ))
        .jsonBody(AuthLoginBody(
            prefs.getString("email", "")!!,
            prefs.getString("password", "")!!,
            "android",
            CLIENT_SECRET,
            prefs.getString("attendeeCode", "")!!
        ))
        .awaitObject(gsonDeserializerOf(AuthLoginResponseToken::class.java))
    prefs.edit {
        putString("jwt", token.jwt)
    }
    return
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
    get() = SecuredPreferenceStore.getSharedInstance().getString("jwt", "") ?: ""
    set(value) = SecuredPreferenceStore.getSharedInstance().edit { putString("jwt", value) }

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
