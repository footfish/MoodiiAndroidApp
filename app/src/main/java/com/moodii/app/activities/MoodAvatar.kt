package com.moodii.app.activities

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatImageButton
import android.support.v7.widget.AppCompatImageView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.moodii.app.R
import com.moodii.app.api.MoodiiApi
import com.moodii.app.models.*
import com.moodii.app.models.AvatarFactory
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.constraint.ConstraintLayout
import android.support.v4.content.FileProvider
import android.view.View
import com.moodii.app.BuildConfig
import com.moodii.app.helpers.OnSwipeTouchListener
import java.io.File
import java.io.FileOutputStream


private var mooder = Mooder("","","", Avatar(), Mood())
private var selectedMood  = NEUTRAL
private var mooderId = "0"
private const val MOODIIURL = "http://www.moodii.com/"

class MoodAvatar : AppCompatActivity() {

    //Add the action buttons to Navbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.mood_avatar, menu)
        return true
    }

    //Handle Navbar actions
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_edit -> {
            val intent = Intent(this, EditAvatar::class.java)
            intent.putExtra("mooderId", mooderId)
            startActivity(intent)
            overridePendingTransition(0, 0) //stop flicker on activity change
            finish()
            true
        }

        R.id.action_signOut -> {
            val intent = Intent(this, SignIn::class.java)
            intent.putExtra("signOut",true)
            startActivity(intent)
            finish()
            true
        }
        R.id.action_shareMoodCloud -> {
            saveMood()
            true
        }
        R.id.action_shareMoodLink -> {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT,  MOODIIURL+"avatar/"+ mooder.hash+".png")
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,  "This is how I feel..")
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, "Share link using"))
            true
        }
        R.id.action_shareMoodImage -> {
            //save to bitmap
            val avatarLayoutView = findViewById<ConstraintLayout>(R.id.avatarLayout)
            val bitmap = viewToBitmap(avatarLayoutView)

            val file = File(this.cacheDir, "myMoodii.png")
            Log.w("MoodAvatar", "Storing to file " + file.toString())
            val output = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.close()
            val sendIntent = Intent()
            val contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file)
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,  "This is how I feel..")
            sendIntent.type = "img/png"
            startActivity(Intent.createChooser(sendIntent, "Share image using"))
            true
        }


        else -> { //action not recognised.
            super.onOptionsItemSelected(item)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_avatar)
        //add navbar
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.title = " " + mooder.nameTag
        if (mooder.nameTag == "") supportActionBar?.setLogo(R.drawable.moodii_logo_sad) else supportActionBar?.setLogo(R.drawable.moodii_logo_happy)

        //set mooderId if passed (from SignIn)
        if(this.intent.hasExtra("mooderId")) mooderId =this.intent.extras.getString("mooderId")


        //set listener for floating share button
        findViewById<FloatingActionButton>(R.id.shareButton).setOnClickListener({saveMood()})

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
        //init array of mood buttons
        val buttonViews = arrayOf<AppCompatImageButton> (
                findViewById(R.id.buttonNeutral),
                findViewById(R.id.buttonHappy),
                findViewById(R.id.buttonSad),
                findViewById(R.id.buttonScared),
                findViewById(R.id.buttonAngry),
                findViewById(R.id.buttonSurprised)
                )

        //load mooder we'll use
        val tMooder = MoodiiApi.getMooder(mooderId)
        if (tMooder != null) {
            mooder = tMooder
        }
        //nts: implement failed load
        Log.w("MoodAvatar", "starting with mooder " + mooder.toString())

        //init buttons
        selectedMood  = AvatarFactory.getMoodInt(mooder.mood.mood)
        setButtonSelected(buttonViews, selectedMood)

        //set button listeners
        for (i in buttonViews.indices) buttonViews[i].setOnClickListener {
            setButtonSelected(buttonViews, i)
            renderMoodAvatar(avatarViews, i)
        }

        //render avatar
        renderMoodAvatar(avatarViews, selectedMood)

        (avatarViews[HEAD] as View).setOnTouchListener(  //avatarViews[] cast as View as View will override performClick (otherwise warning)
                object : OnSwipeTouchListener(this) {
                    override fun onSwipeRight() {
                        if (selectedMood < buttonViews.size-1) selectedMood++ else selectedMood=0
                        setButtonSelected(buttonViews, selectedMood)
                        renderMoodAvatar(avatarViews, selectedMood)
                    }
                    override fun onSwipeLeft() {
                        if (selectedMood > 1 ) selectedMood-- else selectedMood=buttonViews.size-1
                        setButtonSelected(buttonViews, selectedMood)
                        renderMoodAvatar(avatarViews, selectedMood)

                    }
                }
        )
    }

    private fun renderMoodAvatar(avatarViews: Array<AppCompatImageView>, mood: Int) {
        for ( partType in avatarViews.indices) {
            avatarViews[partType].setImageResource(resources.getIdentifier(AvatarFactory.getResPart(mooder.avatar, partType, mood), "drawable", packageName))
            renderPartColor(avatarViews,partType)
        }
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

    private fun setButtonSelected(buttonViews: Array<AppCompatImageButton>, selectedButton: Int) {
        if (!buttonViews[selectedButton].isSelected) {
            selectedMood = selectedButton
            for (i in buttonViews.indices) buttonViews[i].isSelected = (i == selectedButton) //highlights the selected button
            setSharedButton(false)
        }
    }

    private fun saveMood(){
        mooder.mood.mood=AvatarFactory.getMoodString(selectedMood)
        mooder.mood.timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ", Locale.UK).format(Date())


        Log.w("MoodAvatar", "saving with mooder " + mooderId + " " + mooder.mood.toString())
        if (MoodiiApi.updateMood(mooderId, mooder.mood)) {
            Toast.makeText(applicationContext, "Moodii shared", Toast.LENGTH_SHORT).show()
            setSharedButton(true)
        } else {
            Toast.makeText(applicationContext, "Failed to save (Internet connection?)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSharedButton(stored: Boolean) {
        val shareButton = findViewById<FloatingActionButton>(R.id.shareButton)
        if (stored)  shareButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorFloatingButtonSaved))
        else shareButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorFloatingButton))
    }

    fun viewToBitmap(view: View): Bitmap {  //careful where this is called, views must have been created width/height will be zero
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

}