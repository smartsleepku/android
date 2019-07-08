package dk.ku.sund.smartsleep.model

import java.util.*

data class Survey(
    var sid: String,
    var surveyls_title: String,
    var startdate: String,
    var expires: String,
    var active: String
)
