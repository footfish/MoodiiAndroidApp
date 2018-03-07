package com.moodii.app.models

/**
 * data class for storing Avatar
 */

data class Avatar(
        var headId: String = "1",
        var hairTopId: String = "1",
        var hairBackId: String = "1",
        var eyesId: String = "1",
        var noseId: String = "1",
        var mouthId: String = "1",
        var eyebrowsId: String = "1",
        var eyebrowsColor: String = "",
        var skinColor: String = "",
        var hairColor: String = ""
        )