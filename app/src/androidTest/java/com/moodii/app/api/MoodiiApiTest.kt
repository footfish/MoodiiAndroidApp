package com.moodii.app.api

import com.moodii.app.helpers.AvatarFactory
import com.moodii.app.models.Avatar
import com.moodii.app.models.Mood
import org.junit.Test

import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * class MoodiiApiTest tests MoodiiApi
 */

class MoodiiApiTest {
    private val testUid = "test-id" //test-id configured on api for testing

    @Test
    fun updateMood() {
        val testMood = Mood("happy","excited", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ", Locale.UK).format(Date())) //only set fields that can be retrieved
        var moodResult:Mood? = Mood()

        MoodiiApi.updateMood(testUid,testMood) {
            success: Boolean -> //callback
            assertTrue(success)
            moodResult = MoodiiApi.getMooder(testUid)?.mood
        }
        Thread.sleep(5000) //wait 5 secs for async to return.
        assertEquals(moodResult,testMood)
    }

    @Test
    fun updateAvatar(){
        val testAvatar = AvatarFactory.getRandomAvatar()
        var avatarResult:Avatar? = Avatar()
        MoodiiApi.updateAvatar(testUid,testAvatar) {
            success: Boolean -> //callback
            assertTrue(success)
            avatarResult = MoodiiApi.getAvatar(testUid)
        }
        Thread.sleep(5000) //wait 5 secs for async to return.
        assertEquals(avatarResult,testAvatar)
    }
}