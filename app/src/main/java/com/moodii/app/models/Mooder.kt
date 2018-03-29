package com.moodii.app.models

/**
 * Data class for moodii user
 */
data class Mooder(
        var id: String = "", //UUID
        var nameTag: String = "",
        var avatar: Avatar,
        var mood: Mood
)

