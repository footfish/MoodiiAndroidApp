package com.moodii.app.controllers

import com.moodii.app.models.Moodii

/**
 * Created by kevb on 25/02/2018.
 */

class MoodiiApi(id: String) {
    lateinit var moodii:Moodii
    init {
        if (id.isEmpty()) {
            //create empty moodii
        } else {
            //fetch existing moodii
        }
    }

}