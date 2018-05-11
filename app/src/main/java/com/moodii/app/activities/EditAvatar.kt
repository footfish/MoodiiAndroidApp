package com.moodii.app.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import com.moodii.app.models.*
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.Toast
import com.moodii.app.R
import com.moodii.app.api.MoodiiApi
import com.moodii.app.helpers.*
import com.moodii.app.helpers.AvatarFactory.getRandomAvatar
import java.util.*


private var mooder = Mooder("", "",Avatar(), Mood())
private var reloadMooder = true //if true will load from API, esp. used on rotate
private var mooderId = "unknown"
private var selectedPart  = HEAD
private var coloringMode  = false

class EditAvatar : AppCompatActivity() {

    //Add the action buttons to Navbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.edit_avatar, menu)
        return true
    }

    //Handle Navbar actions
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_save -> {
            MoodiiApi.updateAvatar(mooderId, mooder.avatar)
            { success: Boolean -> //callback
                if (success) {
                    val intent = Intent(this, MoodAvatar::class.java)
                    intent.putExtra("mooderId", mooderId)
                    intent.putExtra("reloadMooder", true)
                    startActivity(intent)
                    overridePendingTransition(0, 0) //stop flicker on activity change
                    finish() //forget back button
                } else {
                    Toast.makeText(applicationContext, "Failed to save (Internet connection active?)", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
        R.id.action_quit -> {
            val intent = Intent(this, MoodAvatar::class.java)
            intent.putExtra("mooderId", mooderId)
            intent.putExtra("reloadMooder", true)
            startActivity(intent)
            overridePendingTransition(0, 0) //stop flicker on activity change
            finish() //forget back button
            true
        }
        R.id.action_random -> {
            mooder.avatar = getRandomAvatar()
            //init array of avatar parts
            val avatarViews = arrayOf<AppCompatImageView> (
                    findViewById(R.id.head),
                    findViewById(R.id.hairTop),
                    findViewById(R.id.hairBack),
                    findViewById(R.id.eyes),
                    findViewById(R.id.nose),
                    findViewById(R.id.mouth),
                    findViewById(R.id.eyebrows)
            )
            renderAvatar(avatarViews)
            true
        }
       else -> { //action not recognised.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_avatar)

        //add Nav bar
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.title = " Edit " +  getString(R.string.app_name)
        if (Random().nextBoolean()) {
            supportActionBar?.setLogo(R.drawable.moodii_logo_sad)
        } else {
            supportActionBar?.setLogo(R.drawable.moodii_logo_happy)
        }

        //Check passed intents
        if(this.intent.hasExtra("mooderId")) mooderId =this.intent.extras.getString("mooderId")
        if(this.intent.hasExtra("reloadMooder")) {  //force reload from API after Edit
            reloadMooder = this.intent.extras.getBoolean("reloadMooder")
            this.intent.removeExtra("reloadMooder")
        }

        //init array of avatar parts
        val avatarViews = arrayOf<AppCompatImageView> (
                findViewById(R.id.head),
                findViewById(R.id.hairTop),
                findViewById(R.id.hairBack),
                findViewById(R.id.eyes),
                findViewById(R.id.nose),
                findViewById(R.id.mouth),
                findViewById(R.id.eyebrows)
        )

        //init array of edit buttons
        val buttonViews = arrayOf<AppCompatImageButton> (
                findViewById(R.id.buttonHead),
                findViewById(R.id.buttonHairTop),
                findViewById(R.id.buttonHairBack),
                findViewById(R.id.buttonEyes),
                findViewById(R.id.buttonNose),
                findViewById(R.id.buttonMouth),
                findViewById(R.id.buttonEyebrows)
                )

        //load mooder we'll use
        if (reloadMooder) {
            val tAvatar = MoodiiApi.getAvatar(mooderId)
            if (tAvatar != null) {
                mooder.avatar = tAvatar
                reloadMooder = false
            } else { //can't load return to MoodAvatar
                val intent = Intent(this, MoodAvatar::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0) //stop flicker on activity change
                finish() //forget back button
                Toast.makeText(applicationContext, "Can't edit (Internet connection active?)", Toast.LENGTH_SHORT).show()
            }
        }
        //nts: implement failed load

        //init selected buttons
        toggleColoringMode(findViewById<ImageButton>(R.id.iconFaceToggle))
        setButtonSelected(buttonViews, selectedPart)
        //set button listeners
        for (i in buttonViews.indices) buttonViews[i].setOnClickListener { setButtonSelected(buttonViews,i)}


        renderAvatar(avatarViews)
/*        //render avatar parts
        for (i in avatarViews.indices) renderPart(avatarViews[i],i)
        //render avatar parts colors
        renderPartColor(avatarViews, HEAD)
        renderPartColor(avatarViews, HAIRTOP)
        renderPartColor(avatarViews, EYEBROWS) */


        (avatarViews[HEAD] as View).setOnTouchListener(  //avatarViews[] cast as View as View will override performClick (otherwise warning)
                object : OnSwipeTouchListener(this) {
                    override fun onSwipeRight() {
                        if (!coloringMode) {
                            when(selectedPart) {
                                HEAD -> mooder.avatar.headId = AvatarFactory.getNextPart(mooder.avatar.headId, selectedPart)!!
                                HAIRTOP -> mooder.avatar.hairTopId = AvatarFactory.getNextPart(mooder.avatar.hairTopId, selectedPart)!!
                                HAIRBACK -> mooder.avatar.hairBackId = AvatarFactory.getNextPart(mooder.avatar.hairBackId, selectedPart)!!
                                EYES -> mooder.avatar.eyesId = AvatarFactory.getNextPart(mooder.avatar.eyesId, selectedPart)!!
                                NOSE -> mooder.avatar.noseId = AvatarFactory.getNextPart(mooder.avatar.noseId, selectedPart)!!
                                MOUTH -> mooder.avatar.mouthId = AvatarFactory.getNextPart(mooder.avatar.mouthId, selectedPart)!!
                                EYEBROWS -> mooder.avatar.eyebrowsId = AvatarFactory.getNextPart(mooder.avatar.eyebrowsId, selectedPart)!!
                            }
                            renderPart(avatarViews[selectedPart], selectedPart)
                        } else {
                            when(selectedPart) {
                                HEAD -> mooder.avatar.skinColor = AvatarFactory.getNextPartColor(mooder.avatar.skinColor, selectedPart)!!
                                HAIRTOP -> mooder.avatar.hairColor = AvatarFactory.getNextPartColor(mooder.avatar.hairColor, selectedPart)!!
                                HAIRBACK -> mooder.avatar.hairColor = AvatarFactory.getNextPartColor(mooder.avatar.hairColor, selectedPart)!!
                                EYEBROWS -> mooder.avatar.eyebrowsColor = AvatarFactory.getNextPartColor(mooder.avatar.eyebrowsColor, selectedPart)!!
                            }
                            renderPartColor(avatarViews, selectedPart)
                        }
                    }
                    override fun onSwipeLeft() {
                        if (!coloringMode) {
                            when (selectedPart) {
                                HEAD -> mooder.avatar.headId = AvatarFactory.getPrevPart(mooder.avatar.headId, selectedPart)!!
                                HAIRTOP -> mooder.avatar.hairTopId = AvatarFactory.getPrevPart(mooder.avatar.hairTopId, selectedPart)!!
                                HAIRBACK -> mooder.avatar.hairBackId = AvatarFactory.getPrevPart(mooder.avatar.hairBackId, selectedPart)!!
                                EYES -> mooder.avatar.eyesId = AvatarFactory.getPrevPart(mooder.avatar.eyesId, selectedPart)!!
                                NOSE -> mooder.avatar.noseId = AvatarFactory.getPrevPart(mooder.avatar.noseId, selectedPart)!!
                                MOUTH -> mooder.avatar.mouthId = AvatarFactory.getPrevPart(mooder.avatar.mouthId, selectedPart)!!
                                EYEBROWS -> mooder.avatar.eyebrowsId = AvatarFactory.getPrevPart(mooder.avatar.eyebrowsId, selectedPart)!!
                            }
                            renderPart(avatarViews[selectedPart], selectedPart)
                        } else {
                            when(selectedPart) {
                                HEAD -> mooder.avatar.skinColor = AvatarFactory.getPrevPartColor(mooder.avatar.skinColor, selectedPart)!!
                                HAIRTOP -> mooder.avatar.hairColor = AvatarFactory.getPrevPartColor(mooder.avatar.hairColor, selectedPart)!!
                                HAIRBACK -> mooder.avatar.hairColor = AvatarFactory.getPrevPartColor(mooder.avatar.hairColor, selectedPart)!!
                                EYEBROWS -> mooder.avatar.eyebrowsColor = AvatarFactory.getPrevPartColor(mooder.avatar.eyebrowsColor, selectedPart)!!
                            }
                            renderPartColor(avatarViews, selectedPart)
                        }
                    }
                }
        )
    }

    override fun onStart() {
        super.onStart()
            //show animate swipe icon
            val swipeSplash = findViewById<ImageView>(R.id.iconSwipe)
            val swipeDraw = swipeSplash.drawable
            if (swipeDraw is Animatable) {
                swipeDraw.start()
            }

    }

    /* renderAvatar()  - Renders mood.avatar to avatarViews with neutral mood */
    private fun renderAvatar(avatarViews: Array<AppCompatImageView>) {
        for ( partType in avatarViews.indices) {
            avatarViews[partType].setImageResource(resources.getIdentifier(AvatarFactory.getResPart(mooder.avatar, partType, NEUTRAL), "drawable", packageName))
            renderPartColor(avatarViews,partType)
        }
    }

    //renders the avatars stored part of type 'partType' to the passed ImageView v.
    private fun renderPart(v: ImageView, partType: Int) {
            v.setImageResource(resources.getIdentifier(AvatarFactory.getResPart(mooder.avatar, partType), "drawable", packageName))
    }

    private fun renderPartColor(v: Array<AppCompatImageView>, partType: Int) {
        when(partType) {
            HEAD -> {
                v[HEAD].setColorFilter(Color.parseColor(mooder.avatar.skinColor), PorterDuff.Mode.SRC_ATOP)
            }
            HAIRTOP -> {
                v[HAIRBACK].setColorFilter(Color.parseColor(mooder.avatar.hairColor), PorterDuff.Mode.SRC_ATOP)
                v[HAIRTOP].setColorFilter(Color.parseColor(mooder.avatar.hairColor), PorterDuff.Mode.SRC_ATOP)
            }
            HAIRBACK -> {
                v[HAIRBACK].setColorFilter(Color.parseColor(mooder.avatar.hairColor), PorterDuff.Mode.SRC_ATOP)
                v[HAIRTOP].setColorFilter(Color.parseColor(mooder.avatar.hairColor), PorterDuff.Mode.SRC_ATOP)
            }
            EYEBROWS -> {
                v[EYEBROWS].setColorFilter(Color.parseColor(mooder.avatar.eyebrowsColor), PorterDuff.Mode.SRC_ATOP)
            }
        }

    }

    private fun setButtonSelected(buttonViews: Array<AppCompatImageButton>, selectedButton: Int ) {
        selectedPart = selectedButton
        val colorButton= findViewById<ImageButton>(R.id.iconColorToggle)
         colorButton.isSelected = coloringMode
        for (i in buttonViews.indices) buttonViews[i].isSelected = (i == selectedButton) //highlights the selected button
        when(selectedButton){
            HEAD -> colorButton.visibility = View.VISIBLE
            HAIRTOP -> colorButton.visibility=View.VISIBLE
            HAIRBACK -> colorButton.visibility=View.VISIBLE
            EYEBROWS -> colorButton.visibility=View.VISIBLE
            MOUTH -> {
                colorButton.visibility=View.GONE
                toggleColoringMode(findViewById<ImageButton>(R.id.iconFaceToggle))
            }
            EYES -> {
                colorButton.visibility=View.GONE
                toggleColoringMode(findViewById<ImageButton>(R.id.iconFaceToggle))
            }
            NOSE -> {
                colorButton.visibility=View.GONE
                toggleColoringMode(findViewById<ImageButton>(R.id.iconFaceToggle))
            }
        }
    }

    fun toggleColoringMode(v: View){
        val faceButton = findViewById<ImageButton>(R.id.iconFaceToggle)
        val colorButton =  findViewById<ImageButton>(R.id.iconColorToggle)
        coloringMode = (v == colorButton)
        faceButton.isSelected = !coloringMode
        colorButton.isSelected = coloringMode
    }

}
