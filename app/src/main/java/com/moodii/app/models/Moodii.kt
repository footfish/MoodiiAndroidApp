package com.moodii.app.models

/**
 * Data class for moodii user instance
 */
data class Moodii (
        var id: String = "",
        var avatar: Avatar,
        var mood: Mood
)

