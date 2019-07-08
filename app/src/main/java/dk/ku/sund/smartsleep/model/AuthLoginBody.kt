package dk.ku.sund.smartsleep.model

data class AuthLoginBody(
    var email: String,
    var password: String,
    var clientId: String,
    var clientSecret: String,
    var attendeeCode: String
)
