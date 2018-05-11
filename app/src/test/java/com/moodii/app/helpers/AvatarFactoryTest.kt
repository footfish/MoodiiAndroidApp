package com.moodii.app.helpers

import org.junit.Test

import org.junit.Assert.*

/**
 * Class AvatarFactoryTest tests AvatarFactory
 */
const val INVALID_PARTID = "nothing"
const val INVALID_PART = 500000
const val INVALID_MOOD_INDEX = 500000
const val INVALID_MOOD_STRING = "happy-sad"
const val HEAD = 0

class AvatarFactoryTest {
    private val moods  = arrayOf ("neutral","happy","sad","scared","angry","surprised")

    @Test
    fun getPrevPart() {
        assertNull(AvatarFactory.getPrevPart(INVALID_PARTID, INVALID_PART)) //part type out of bounds
        assertNull(AvatarFactory.getPrevPart(INVALID_PARTID,-INVALID_PART)) //part type out of bounds
        assertNotEquals("",AvatarFactory.getPrevPart(INVALID_PARTID,HEAD)) //invalid partid (should return first part)
        //test getting of valid part
        val firstPart = AvatarFactory.getPrevPart(INVALID_PARTID,HEAD)   //invalid partid returns first part
        val nextPart = AvatarFactory.getNextPart(firstPart!!,HEAD)         //get next
        assertEquals(firstPart,AvatarFactory.getPrevPart(nextPart!!,HEAD)) //get prev from next and test
        //test across array boundary
        val prevPart = AvatarFactory.getPrevPart(firstPart,HEAD)         //get prev across boundary
        assertEquals(firstPart,AvatarFactory.getNextPart(prevPart!!,HEAD)) //get next (back to first) and test
    }

    @Test
    fun getNextPart() {
        assertNull(AvatarFactory.getNextPart(INVALID_PARTID,INVALID_PART)) //part type out of bounds
        assertNull(AvatarFactory.getNextPart(INVALID_PARTID,-INVALID_PART)) //part type out of bounds
        assertNotEquals("",AvatarFactory.getNextPart(INVALID_PARTID,HEAD)) //invalid partid (should return first part)
        //test getting of valid part
        val firstPart = AvatarFactory.getNextPart(INVALID_PARTID,HEAD)   //invalid partid returns first part
        val nextPart = AvatarFactory.getNextPart(firstPart!!,HEAD)         //get next
        assertEquals(firstPart,AvatarFactory.getPrevPart(nextPart!!,HEAD)) //get prev from next and test
        //test across array boundary
        val prevPart = AvatarFactory.getPrevPart(firstPart,HEAD)         //get prev across boundary
        assertEquals(firstPart,AvatarFactory.getNextPart(prevPart!!,HEAD)) //get next (back to first) and test
    }

    @Test
    fun getPrevPartColor() {
        assertNull(AvatarFactory.getPrevPartColor(INVALID_PARTID,INVALID_PART))  //part type out of bounds
        assertNull(AvatarFactory.getPrevPartColor(INVALID_PARTID,-INVALID_PART))  //part type out of bounds
        assertNotEquals("",AvatarFactory.getPrevPartColor(INVALID_PARTID,HEAD)) //invalid partid (should return first part color)
        //test getting valid part color
        val firstPartColor = AvatarFactory.getPrevPartColor(INVALID_PARTID,HEAD)   //invalid partid returns first part color
        val nextPartColor = AvatarFactory.getNextPartColor(firstPartColor!!,HEAD)         //get next
        assertEquals(firstPartColor,AvatarFactory.getPrevPartColor(nextPartColor!!,HEAD)) //get prev from next and test
        //test across array boundary
        val prevPartColor = AvatarFactory.getPrevPartColor(firstPartColor,HEAD)         //get prev across boundary
        assertEquals(firstPartColor,AvatarFactory.getNextPartColor(prevPartColor!!,HEAD)) //get next from prev and test
    }

    @Test
    fun getNextPartColor() {
        assertNull(AvatarFactory.getNextPartColor(INVALID_PARTID,INVALID_PART))  //part type out of bounds
        assertNull(AvatarFactory.getNextPartColor(INVALID_PARTID,-INVALID_PART))  //part type out of bounds
        assertNotEquals("",AvatarFactory.getNextPartColor(INVALID_PARTID,HEAD)) //invalid partid (should return first part color)
        //test getting valid part color
        val firstPartColor = AvatarFactory.getNextPartColor(INVALID_PARTID,HEAD)   //invalid partid returns first part color
        val nextPartColor = AvatarFactory.getNextPartColor(firstPartColor!!,HEAD)         //get next
        assertEquals(firstPartColor,AvatarFactory.getPrevPartColor(nextPartColor!!,HEAD)) //get prev from next and test
        //test across array boundary
        val prevPartColor = AvatarFactory.getPrevPartColor(firstPartColor,HEAD)         //get prev across boundary
        assertEquals(firstPartColor,AvatarFactory.getNextPartColor(prevPartColor!!,HEAD)) //get next from prev and test
    }

    @Test
    fun getMoodString() {
        assertEquals(moods[NEUTRAL], AvatarFactory.getMoodString(INVALID_MOOD_INDEX)) //mood out of bounds
        assertEquals(moods[NEUTRAL], AvatarFactory.getMoodString(-INVALID_MOOD_INDEX)) //mood out of bounds
        for (i in moods.indices) {
            assertEquals(moods[i], AvatarFactory.getMoodString(i))
        }
    }

    @Test
    fun getMoodInt() {
        assertEquals(NEUTRAL,AvatarFactory.getMoodInt(INVALID_MOOD_STRING))
        for (i in moods.indices) {
            assertEquals(i, AvatarFactory.getMoodInt(moods[i]))
        }

    }


}