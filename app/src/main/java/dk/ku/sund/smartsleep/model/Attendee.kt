package dk.ku.sund.smartsleep.model

import java.util.*

data class Attendee(
    var gmtOffset: Int,
    var weekdayMorning: Date?,
    var weekdayEvening: Date?,
    var weekendMorning: Date?,
    var weekendEvening: Date?
)

data class DebugInfo(
    var time: Date?,
    var model: String?,
    var manufacturer: String?,
    var systemVersion: String?,
    var systemName: String?
)