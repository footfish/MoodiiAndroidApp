package com.moodii.app.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import com.moodii.app.helpers.OnSwipeTouchListener
import com.moodii.app.models.*
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.moodii.app.MoodiiApp
import com.moodii.app.R
import com.moodii.app.api.MoodiiApi


private var mooder = Mooder("", "","", Avatar(), Mood())
private var mooderId = "0"

private var selectedPart  = HEAD
private var coloringMode  = false

class EditAvatar : AppCompatActivity() {
    lateinit var app: MoodiiApp
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Add the action buttons to Navbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.edit_avatar, menu)
        return true
    }

    //Handle Navbar actions
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_save -> {
            if (MoodiiApi.updateAvatar(mooderId, mooder.avatar)) {

                //location
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION )
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted

                    //request permission
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            4501)
//                            MY_PERMISSION_ACCESS_COURSE_LOCATION)

                }

                fusedLocationClient.lastLocation
                        .addOnSuccessListener { location : Location? ->
                            Log.w("EditAvatar", "locaton" + location.toString())
                            // Got last known location. In some rare situations this can be null.
                        }


                val intent = Intent(this, MoodAvatar::class.java)
                intent.putExtra("mooderId", mooderId)
                startActivity(intent)
                overridePendingTransition(0, 0) //stop flicker on activity change
                finish() //forget back button
                var nameTagView = findViewById<EditText>(R.id.textNameTag)
                mooder.nameTag = nameTagView.text.toString()
            } else {
                Toast.makeText(applicationContext, "Failed to save (Internet connection active?)", Toast.LENGTH_SHORT).show()
            }
            true
        }

        R.id.action_quit -> {
            val intent = Intent(this, MoodAvatar::class.java)
            intent.putExtra("mooderId", mooderId)
            startActivity(intent)
            overridePendingTransition(0, 0) //stop flicker on activity change
            finish() //forget back button
            true
        }
       else -> { //action not recognised.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        app = application as MoodiiApp
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_avatar)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //add Nav bar
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.title = ""
            if (mooder.nameTag == "") supportActionBar?.setLogo(R.drawable.moodii_logo_sad) else supportActionBar?.setLogo(R.drawable.moodii_logo_happy)

        //set mooderId if passed
        if(this.intent.hasExtra("mooderId")) mooderId =this.intent.extras.getString("mooderId")

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
        val tAvatar = MoodiiApi.getAvatar(mooderId)
        if (tAvatar != null) {
            mooder.avatar = tAvatar
        } else { //can't load return to MoodAvatar
            val intent = Intent(this, MoodAvatar::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0) //stop flicker on activity change
            finish() //forget back button
            Toast.makeText(applicationContext, "Can't edit (Internet connection active?)", Toast.LENGTH_SHORT).show()
        }

        //nts: implement failed load
        Log.w("EditAvatar", "starting with mooder " + mooder.toString())

        val iconToggleColorView= findViewById<ImageButton>(R.id.iconColorToggle)
        //init selected button
        setButtonSelected(buttonViews, selectedPart,iconToggleColorView)
        //set button listeners
        for (i in buttonViews.indices) buttonViews[i].setOnClickListener { setButtonSelected(buttonViews,i,iconToggleColorView)}

        //render avatar parts
        for (i in avatarViews.indices) renderPart(avatarViews[i],i)
        //render avatar parts colors
        renderPartColor(avatarViews, HEAD)
        renderPartColor(avatarViews, HAIRTOP)
        renderPartColor(avatarViews, EYEBROWS)
        //render name tag
        var nameTagView = findViewById<EditText>(R.id.textNameTag)
        nameTagView.setText(mooder.nameTag)


            avatarViews[HEAD].setOnTouchListener(
                object : OnSwipeTouchListener(this) {
                    override fun onSwipeRight() {
                        if (!coloringMode) {
                            when(selectedPart) {
                                HEAD -> mooder.avatar.headId = AvatarFactory.getNextPart(mooder.avatar.headId, selectedPart)
                                HAIRTOP -> mooder.avatar.hairTopId = AvatarFactory.getNextPart(mooder.avatar.hairTopId, selectedPart)
                                HAIRBACK -> mooder.avatar.hairBackId = AvatarFactory.getNextPart(mooder.avatar.hairBackId, selectedPart)
                                EYES -> mooder.avatar.eyesId = AvatarFactory.getNextPart(mooder.avatar.eyesId, selectedPart)
                                NOSE -> mooder.avatar.noseId = AvatarFactory.getNextPart(mooder.avatar.noseId, selectedPart)
                                MOUTH -> mooder.avatar.mouthId = AvatarFactory.getNextPart(mooder.avatar.mouthId, selectedPart)
                                EYEBROWS -> mooder.avatar.eyebrowsId = AvatarFactory.getNextPart(mooder.avatar.eyebrowsId, selectedPart)
                            }
                            renderPart(avatarViews[selectedPart], selectedPart)
                        } else {
                            when(selectedPart) {
                                HEAD -> mooder.avatar.skinColor = AvatarFactory.getNextPartColor(mooder.avatar.skinColor, selectedPart)
                                HAIRTOP -> mooder.avatar.hairColor = AvatarFactory.getNextPartColor(mooder.avatar.hairColor, selectedPart)
                                HAIRBACK -> mooder.avatar.hairColor = AvatarFactory.getNextPartColor(mooder.avatar.hairColor, selectedPart)
                                EYEBROWS-> mooder.avatar.eyebrowsColor = AvatarFactory.getNextPartColor(mooder.avatar.eyebrowsColor, selectedPart)
                            }
                            renderPartColor(avatarViews, selectedPart)
                        }
                    }
                    override fun onSwipeLeft() {
                        if (!coloringMode) {
                            when (selectedPart) {
                                HEAD -> mooder.avatar.headId = AvatarFactory.getPrevPart(mooder.avatar.headId, selectedPart)
                                HAIRTOP -> mooder.avatar.hairTopId = AvatarFactory.getPrevPart(mooder.avatar.hairTopId, selectedPart)
                                HAIRBACK -> mooder.avatar.hairBackId = AvatarFactory.getPrevPart(mooder.avatar.hairBackId, selectedPart)
                                EYES -> mooder.avatar.eyesId = AvatarFactory.getPrevPart(mooder.avatar.eyesId, selectedPart)
                                NOSE -> mooder.avatar.noseId = AvatarFactory.getPrevPart(mooder.avatar.noseId, selectedPart)
                                MOUTH -> mooder.avatar.mouthId = AvatarFactory.getPrevPart(mooder.avatar.mouthId, selectedPart)
                                EYEBROWS -> mooder.avatar.eyebrowsId = AvatarFactory.getPrevPart(mooder.avatar.eyebrowsId, selectedPart)
                            }
                            renderPart(avatarViews[selectedPart], selectedPart)
                        } else {
                            when(selectedPart) {
                                HEAD -> mooder.avatar.skinColor = AvatarFactory.getPrevPartColor(mooder.avatar.skinColor, selectedPart)
                                HAIRTOP -> mooder.avatar.hairColor = AvatarFactory.getPrevPartColor(mooder.avatar.hairColor, selectedPart)
                                HAIRBACK -> mooder.avatar.hairColor = AvatarFactory.getPrevPartColor(mooder.avatar.hairColor, selectedPart)
                                EYEBROWS-> mooder.avatar.eyebrowsColor = AvatarFactory.getPrevPartColor(mooder.avatar.eyebrowsColor, selectedPart)
                            }
                            renderPartColor(avatarViews, selectedPart)
                        }
                    }
                }
        )
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
            HAIRBACK-> {
                v[HAIRBACK].setColorFilter(Color.parseColor(mooder.avatar.hairColor), PorterDuff.Mode.SRC_ATOP)
                v[HAIRTOP].setColorFilter(Color.parseColor(mooder.avatar.hairColor), PorterDuff.Mode.SRC_ATOP)
            }
            EYEBROWS -> {
                v[EYEBROWS].setColorFilter(Color.parseColor(mooder.avatar.eyebrowsColor), PorterDuff.Mode.SRC_ATOP)
            }
        }

    }

    private fun setButtonSelected(buttonViews: Array<AppCompatImageButton>, selectedButton: Int, iconToggleColorView: ImageView) {
        selectedPart = selectedButton
         iconToggleColorView.isSelected = coloringMode
        for (i in buttonViews.indices) buttonViews[i].isSelected = (i == selectedButton) //highlights the selected button
        when(selectedButton){
            HEAD -> iconToggleColorView.visibility = View.VISIBLE
            HAIRTOP -> iconToggleColorView.visibility=View.VISIBLE
            HAIRBACK-> iconToggleColorView.visibility=View.VISIBLE
            EYEBROWS -> iconToggleColorView.visibility=View.VISIBLE
            MOUTH -> {
                iconToggleColorView.visibility=View.GONE
                coloringMode = false
            }
            EYES -> {
                iconToggleColorView.visibility=View.GONE
                coloringMode = false
            }
            NOSE -> {
                iconToggleColorView.visibility=View.GONE
                coloringMode = false
            }
        }
    }

    fun toggleColoringMode(v: View){
        coloringMode = !coloringMode
        v.isSelected = coloringMode
    }


}
