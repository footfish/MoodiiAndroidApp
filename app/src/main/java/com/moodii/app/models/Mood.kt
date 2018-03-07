package com.moodii.app.models

import java.security.MessageDigest
import java.sql.Timestamp
import java.util.*

/**
 * Data class for storing mood
 */

data class Mood(
        var mood: String = "",
        var shortMessage: String = "",
        var timestamp: Timestamp? = null,
        var IpV4: String? = null,
        var IpV6: String? = null,
        var country: String? = null,
        var longtitude: Float = 0.0f,
        var latitude: Float = 0.0f
//NTS - best practice for sharing location  ?
        )