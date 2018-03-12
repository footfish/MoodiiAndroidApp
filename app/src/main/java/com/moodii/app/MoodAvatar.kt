package com.moodii.app

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NO_ANIMATION
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.moodii.app.models.*

//val avatar = Avatar() //nts: temp, move to main
private var selectedMood  = NEUTRAL


class MoodAvatar : AppCompatActivity() {


    //Add the action buttons to Navbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.mood_avatar, menu)
        return true
    }

    //Navbar actions
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_edit -> {
            val intent = Intent(this, EditAvatar::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0) //stop flicker on activity change
            true
        }

        R.id.action_quit -> {
            Log.d("Action", "Quit")
            true
        }
       else -> { //action not recognised.
            super.onOptionsItemSelected(item)
        }
    }
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_avatar)
        setSupportActionBar(findViewById(R.id.my_toolbar)) //add navbar

        val avatarViews = arrayOf<AppCompatImageView> (
                findViewById(R.id.head),
                findViewById(R.id.hairTop),
                findViewById(R.id.hairBack),
                findViewById(R.id.eyes),
                findViewById(R.id.nose),
                findViewById(R.id.mouth),
                findViewById(R.id.eyebrows)
        )

        val buttonViews = arrayOf<AppCompatImageButton> (
                findViewById(R.id.buttonNeutral),
                findViewById(R.id.buttonHappy),
                findViewById(R.id.buttonSad),
                findViewById(R.id.buttonScared),
                findViewById(R.id.buttonAngry),
                findViewById(R.id.buttonSurprised)
                )

        //init selected button
        setButtonSelected(buttonViews, selectedMood)

        //set button listeners
        for (i in NEUTRAL until buttonViews.size) buttonViews[i].setOnClickListener {
            setButtonSelected(buttonViews,i)
            renderMoodAvatar(avatarViews, i)
        }

        //render avatar
        renderMoodAvatar(avatarViews, NEUTRAL)

/*
        avatarViews[HEAD].setOnTouchListener(
                object : OnSwipeTouchListener(this) {
                    override fun onSwipeRight() {
                        if (!coloringMode) {
                            when(selectedPart) {
                                HEAD -> avatar.headId = AvatarFactory.getNextPart(avatar.headId, selectedPart)
                                HAIRTOP -> avatar.hairTopId = AvatarFactory.getNextPart(avatar.hairTopId, selectedPart)
                                HAIRBACK -> avatar.hairBackId = AvatarFactory.getNextPart(avatar.hairBackId, selectedPart)
                                EYES -> avatar.eyesId = AvatarFactory.getNextPart(avatar.eyesId, selectedPart)
                                NOSE -> avatar.noseId = AvatarFactory.getNextPart(avatar.noseId, selectedPart)
                                MOUTH -> avatar.mouthId = AvatarFactory.getNextPart(avatar.mouthId, selectedPart)
                                EYEBROWS -> avatar.eyebrowsId = AvatarFactory.getNextPart(avatar.eyebrowsId, selectedPart)
                            }
                            renderPart(avatarViews[selectedPart], selectedPart)
                        } else {
                            when(selectedPart) {
                                HEAD -> avatar.skinColor = AvatarFactory.getNextPartColor(avatar.skinColor, selectedPart)
                                HAIRTOP -> avatar.hairColor = AvatarFactory.getNextPartColor(avatar.hairColor, selectedPart)
                                HAIRBACK -> avatar.hairColor = AvatarFactory.getNextPartColor(avatar.hairColor, selectedPart)
                                EYEBROWS -> avatar.eyebrowsColor = AvatarFactory.getNextPartColor(avatar.eyebrowsColor, selectedPart)
                            }
                            renderPartColor(avatarViews, selectedPart)
                        }
                    }
                    override fun onSwipeLeft() {
                        if (!coloringMode) {
                            when (selectedPart) {
                                HEAD -> avatar.headId = AvatarFactory.getPrevPart(avatar.headId, selectedPart)
                                HAIRTOP -> avatar.hairTopId = AvatarFactory.getPrevPart(avatar.hairTopId, selectedPart)
                                HAIRBACK -> avatar.hairBackId = AvatarFactory.getPrevPart(avatar.hairBackId, selectedPart)
                                EYES -> avatar.eyesId = AvatarFactory.getPrevPart(avatar.eyesId, selectedPart)
                                NOSE -> avatar.noseId = AvatarFactory.getPrevPart(avatar.noseId, selectedPart)
                                MOUTH -> avatar.mouthId = AvatarFactory.getPrevPart(avatar.mouthId, selectedPart)
                                EYEBROWS -> avatar.eyebrowsId = AvatarFactory.getPrevPart(avatar.eyebrowsId, selectedPart)
                            }
                            renderPart(avatarViews[selectedPart], selectedPart)
                        } else {
                            when(selectedPart) {
                                HEAD -> avatar.skinColor = AvatarFactory.getPrevPartColor(avatar.skinColor, selectedPart)
                                HAIRTOP -> avatar.hairColor = AvatarFactory.getPrevPartColor(avatar.hairColor, selectedPart)
                                HAIRBACK -> avatar.hairColor = AvatarFactory.getPrevPartColor(avatar.hairColor, selectedPart)
                                EYEBROWS -> avatar.eyebrowsColor = AvatarFactory.getPrevPartColor(avatar.eyebrowsColor, selectedPart)
                            }
                            renderPartColor(avatarViews, selectedPart)
                        }
                    }
                }
        ) */
    }

    private fun renderMoodAvatar(avatarViews: Array<AppCompatImageView>, mood: Int) {
        for ( partType in HEAD until avatarViews.size) {
            avatarViews[partType].setImageResource(resources.getIdentifier(AvatarFactory.getResPart(avatar, partType, mood), "drawable", packageName))
            renderPartColor(avatarViews,partType)
        }
    }

    private fun renderPartColor(v: Array<AppCompatImageView>, partType: Int) {
        when(partType) {
            HEAD -> {
                v[HEAD].setColorFilter(Color.parseColor(avatar.skinColor), PorterDuff.Mode.SRC_ATOP)
            }
            HAIRTOP -> {
                v[HAIRBACK].setColorFilter(Color.parseColor(avatar.hairColor), PorterDuff.Mode.SRC_ATOP)
                v[HAIRTOP].setColorFilter(Color.parseColor(avatar.hairColor), PorterDuff.Mode.SRC_ATOP)
            }
            HAIRBACK-> {
                v[HAIRBACK].setColorFilter(Color.parseColor(avatar.hairColor), PorterDuff.Mode.SRC_ATOP)
                v[HAIRTOP].setColorFilter(Color.parseColor(avatar.hairColor), PorterDuff.Mode.SRC_ATOP)
            }
            EYEBROWS -> {
                v[EYEBROWS].setColorFilter(Color.parseColor(avatar.eyebrowsColor), PorterDuff.Mode.SRC_ATOP)
            }
        }

    }

    private fun setButtonSelected(buttonViews: Array<AppCompatImageButton>, selectedButton: Int) {
        selectedMood = selectedButton
        for (i in NEUTRAL until buttonViews.size) buttonViews[i].isSelected = (i == selectedButton) //highlights the selected button
        }

}