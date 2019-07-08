package dk.ku.sund.smartsleep.model

data class SurveyRequest(
    var id: String,
    var method: String,
    var params: Array<String>
)
