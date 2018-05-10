package com.moodii.app.models

/**
 * data class Mood - Model for storing mood and REST API params
 */

data class Mood(
        var mood: String = "neutral",
        var shout: String = "",
        var timestamp: String = "",  //YYYY-MM-DDTHH:mm:ss.sssZ or ±YYYYYY-MM-DDTHH:mm:ss.sssZ - simplified extended ISO 8601 format - timezone is always zero UTC offset (Javascripts date's toJSON method)
        var countryCode: String = "",  //iso code
        var latitude: Double = 0.0,
        var longitude: Double = 0.0
        )