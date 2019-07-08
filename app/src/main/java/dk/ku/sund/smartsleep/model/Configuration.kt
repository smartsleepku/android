package dk.ku.sund.smartsleep.model

import java.util.*

data class Configuration (
    var weekdayMorning: Date,
    var weekdayEvening: Date,
    var weekendMorning: Date,
    var weekendEvening: Date
)
