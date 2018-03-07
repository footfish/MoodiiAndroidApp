package com.moodii.app.models

import com.moodii.app.R.id.head
import com.moodii.app.R.id.parent

const val HEAD = 0
const val HAIRTOP = 1
const val HAIRBACK = 2
const val EYES = 3
const val NOSE = 4
const val MOUTH = 5
const val EYEBROWS = 6
private const val SKINC = 0
private const val HAIRC = 1


/**
 * Created by kevb on 25/02/2018.
 */
object AvatarFactory { //set of avatar part id's (svg's)
    val parts = arrayOf ( setOf("1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24"), //HEAD
            setOf("1","2","3","4","5","6","7"), //HAIRTOP
            setOf("1","2","3","4","5","6","7"), //HAIRBACK
            setOf("1","2","3"), //EYES
            setOf("1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19"), //NOSE
            setOf("1","2","3"), //MOUTH
            setOf("1","2","3","4") //EYEBROWS
            )

    val partcolors = arrayOf (
            setOf("#f7e8a9","#ffd1ab","#ffd2d2","#d8e6b3","#ffd29d","#decbbc","#d2bcab", "#e3bb8d", "#cab26a", "#f9a824", "#dfa26f", "#d38d6f", "#c8909d", "#d5784d", "#b3723d","#a77b67", "#9a5b40", "#7c574f", "#854917", "#864d2c", "#723900", "#593b2b", "#42241a", "#3d0101" ), //SKINC
            setOf("#fffa84","#fbed66","#ffe12b","#e3dc3a","#e3dc3a","#c48647","#cc7227","#e76711","#a3672b","#a95712","#844b11","#733a00","#502f0c","#24150a","#090909","#cdc9d8","#dfe0df","#afbbe4", "#555755","#7c3030","#632929","#9f0000","#3a1212","#3d0101","#65245b","#811943","#e31d1d","#8524fc","#263ce3","#f94dac") //HAIRC
    )

            // Returns the full resource name
    fun getResPart(avatar: Avatar, partType:Int): String {
        return( when (partType){
            HEAD -> "head_" + avatar.headId
            HAIRTOP -> "hair_top_" + avatar.hairTopId
            HAIRBACK -> "hair_back_" + avatar.hairBackId
            EYES -> "eyes_neutral_" + avatar.eyesId
            NOSE -> "nose_" + avatar.noseId
            MOUTH -> "mouth_neutral_" + avatar.mouthId
            EYEBROWS -> "eyebrows_neutral_" + avatar.eyebrowsId
            else -> ""
        })
    }

    //get's the previous part (of type partType) in the parts array in circular fashion
    fun getPrevPart(partId: String, partType: Int): String{
        return(getPrev(parts[partType], partId))
    }

    //get's the next part (of type partType) in the parts array in circular fashion
    fun getNextPart(id: String, partType: Int): String{
        return(getNext(parts[partType], id))
    }

    //get's the previous part (of type partType) in the partcolors array in circular fashion
    fun getPrevPartColor(id: String, partType: Int): String {
        return (when (partType) {
            HEAD -> getPrev(partcolors[SKINC], id)
            HAIRTOP -> getPrev(partcolors[HAIRC], id)
            HAIRBACK -> getPrev(partcolors[HAIRC], id)
            EYEBROWS -> getPrev(partcolors[HAIRC], id)
            else -> ""
        })
    }

    //get's the next part (of type partType) in the partscolors array in circular fashion
    fun getNextPartColor(id: String, partType: Int): String{
        return (when (partType) {
            HEAD -> getNext(partcolors[SKINC], id)
            HAIRTOP -> getNext(partcolors[HAIRC], id)
            HAIRBACK -> getNext(partcolors[HAIRC], id)
            EYEBROWS -> getNext(partcolors[HAIRC], id)
            else -> ""
        })
    }

    //Gets the next 'id' from the set in circular fashion
    private fun getNext(set: Set<String>, id: String): String {
        val i = set.indexOf(id)
        return (if (i < set.size-1 ) set.elementAt(i+1) else set.elementAt(0))
    }

    //Gets the previous 'id' from the set in circular fashion
    private fun getPrev(set: Set<String>, id: String): String {
        val i = set.indexOf(id)
        return( if (i > 0 ) set.elementAt(i-1) else set.elementAt(set.size-1))
    }
}
