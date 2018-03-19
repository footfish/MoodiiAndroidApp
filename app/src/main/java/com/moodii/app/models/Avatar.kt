package com.moodii.app.models

/**
 * data class for storing Avatar
 */

data class Avatar(
        var nameTag: String = "",
        var headId: String = "1",
        var hairTopId: String = "1",
        var hairBackId: String = "1",
        var eyesId: String = "1",
        var noseId: String = "1",
        var mouthId: String = "1",
        var eyebrowsId: String = "1",
        var eyebrowsColor: String = "#502f0c",
        var skinColor: String = "#e3bb8d",
        var hairColor: String = "#502f0c"
        )