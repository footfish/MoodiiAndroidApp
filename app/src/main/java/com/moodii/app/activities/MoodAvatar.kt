package com.moodii.app.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.telephony.TelephonyManager
import android.view.View
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.moodii.app.BuildConfig
import com.moodii.app.helpers.*
import java.io.File
import java.io.FileOutputStream


private var mooder = Mooder("","",Avatar(), Mood())
private var selectedMood  = -1 //-1 = unset, forces load from api
private var reloadMooder = true //  //if true will load from API, esp. used on rotate
private var mooderId = "0"
private const val MOODIISHAREURL = "http://www.moodii.com/me/"
private const val REQUEST_CODE_ACCESS_COURSE_LOCATION = 5401

class MoodAvatar : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
            intent.putExtra("reloadMooder", true)
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
        R.id.action_help -> {
            val intent = Intent(this, Help::class.java)
            startActivity(intent)
            true
        }
        R.id.action_shareMoodCloud -> {
            shareMood()
            true
        }
        R.id.action_shareMoodLink -> {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT,  MOODIISHAREURL + mooder.hash+".png")
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,  "This is how I feel..")
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, "Share link using"))
            true
        }
        R.id.action_shareMoodImage -> {
            //save bitmap to cache file
            val avatarLayoutView = findViewById<ConstraintLayout>(R.id.avatarLayout)
            val bitmap = viewToBitmap(avatarLayoutView)
            val file = File(this.cacheDir, "myMoodii.png")
            val output = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            output.close()
            //share image file with intent
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //add navbar
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar?.title = " My " +  getString(R.string.app_name)
        if (Random().nextBoolean()) {
            supportActionBar?.setLogo(R.drawable.moodii_logo_sad)
        } else {
            supportActionBar?.setLogo(R.drawable.moodii_logo_happy)
        }

        //check intents
        if(this.intent.hasExtra("mooderId")) mooderId =this.intent.extras.getString("mooderId") //from SignIn
        if(this.intent.hasExtra("reloadMooder")) { //force reload from API after Edit
            reloadMooder = this.intent.extras.getBoolean("reloadMooder")
            this.intent.removeExtra("reloadMooder")
        }

                //set listener for floating share button
        findViewById<FloatingActionButton>(R.id.shareButton).setOnClickListener({shareMood()})

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


        //load mood
        if (reloadMooder) {
            val tMooder = MoodiiApi.getMooder(mooderId)
            if (tMooder != null) mooder = tMooder
            reloadMooder = false
        }

        //nts: implement failed load
        Log.w("MoodAvatar", "starting with mooder " + mooder.toString())

        //init buttons
        if (selectedMood == -1) selectedMood  = AvatarFactory.getMoodInt(mooder.mood.mood)
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


    override fun onStart() {
        super.onStart()

        // Check permission for location and request it if not already obtained (better for user flow to do this here instead of during share)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, check if we need explanation
             if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) { //if app requested this permission before and user turned down
                    AlertDialog.Builder(this)
                     .setCancelable(false)
                     .setTitle(getString(R.string.dialog_location_permission_title))
                     .setMessage(getString(R.string.dialog_location_permission_text))
                     .setPositiveButton(getString(R.string.dialog_location_permission_button_settings)) { _ , _ ->
                        ActivityCompat.requestPermissions(this,
                                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                                REQUEST_CODE_ACCESS_COURSE_LOCATION)
                     }
                     .setNegativeButton(getString(R.string.dialog_location_permission_button_dismiss)) { _ , _ -> }
                     .create()
                     .show()
            } else { //if first time app is run OR 'never ask again' clicked
            ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_CODE_ACCESS_COURSE_LOCATION)
            }
         }
    }

/* renderMoodAvatar()  - Render mood.avatar to avatarViews with appropriate mood */
private fun renderMoodAvatar(avatarViews: Array<AppCompatImageView>, mood: Int) {
for ( partType in avatarViews.indices) {
    avatarViews[partType].setImageResource(resources.getIdentifier(AvatarFactory.getResPart(mooder.avatar, partType, mood), "drawable", packageName))
    renderPartColor(avatarViews,partType)
}
}

    //renderPartColor() - Applies color from mooder.avatar to partType in avatar ImageView array
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

/* setButtonSelected() - Highlights the selected mood button */
private fun setButtonSelected(buttonViews: Array<AppCompatImageButton>, selectedButton: Int) {
if (!buttonViews[selectedButton].isSelected) {
    selectedMood = selectedButton
    for (i in buttonViews.indices) buttonViews[i].isSelected = (i == selectedButton) //highlights the selected button
    setSharedButton(false)
}
}

/* shareMood() - Prepares mood data model then saves to api*/
private fun shareMood(){
mooder.mood.mood= AvatarFactory.getMoodString(selectedMood)
mooder.mood.timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSSZ", Locale.UK).format(Date())

//save cc
val tm = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
if (tm.simCountryIso != "") {
    mooder.mood.countryCode = tm.simCountryIso.toLowerCase()
} else {
    mooder.mood.countryCode = Locale.getDefault().country.toLowerCase() //default to locale if there's no sim iso
}

//location
if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) { //If we have permission for access to location
        fusedLocationClient.lastLocation.addOnSuccessListener  { location ->   //callback with location
            if (location != null){
                mooder.mood.latitude = location.latitude
                mooder.mood.longitude = location.longitude
            }
            saveMood()
        }
} else { //share with no location
    saveMood()
}
}

/* saveMood() - Saves mood data model to api*/
private fun saveMood(){
    Log.w("MoodAvatar", "saving with mooder " + mooderId + " " + mooder.mood.toString())
    MoodiiApi.updateMood(mooderId, mooder.mood)
    {success: Boolean -> //callback
        if (success) {
            Toast.makeText(applicationContext, "Moodii shared", Toast.LENGTH_SHORT).show()
        }
        else {
            Toast.makeText(applicationContext, "Failed to save (Internet connection?)", Toast.LENGTH_SHORT).show()
        }
    }
}

/* setSharedButton() - toggles highlight on floating shared button */
private fun setSharedButton(stored: Boolean) {
val shareButton = findViewById<FloatingActionButton>(R.id.shareButton)
if (stored)  {
    shareButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorFloatingButtonSaved))
}
else {
    shareButton.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorFloatingButton))
}
}

/* viewToBitmap() - converts view to a bitmap, used for image sharing
* **CAREFUL** where this is called, views must have been created or width/height will be zero
* */
private fun viewToBitmap(view: View): Bitmap {  //careful where this is called, views must have been created width/height will be zero
val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
val canvas = Canvas(bitmap)
view.draw(canvas)
return bitmap
}

}