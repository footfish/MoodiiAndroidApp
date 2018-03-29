package com.moodii.app.controllers

import com.moodii.app.models.Mooder

/**
 * Created by kevb on 25/02/2018.
 */

class MoodiiApi(id: String) {
    lateinit var mooder: Mooder
    init {
        if (id.isEmpty()) {
            //create empty mooder
        } else {
            //fetch existing mooder
        }
    }

}