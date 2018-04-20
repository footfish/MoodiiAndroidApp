package com.moodii.app.models

/**
 * Data class for moodii user
 */
data class Mooder(
        var id: String = "", //UID
        var hash: String = "", //hash of email address
        var nameTag: String = "",
        var avatar: Avatar,
        var mood: Mood
)

