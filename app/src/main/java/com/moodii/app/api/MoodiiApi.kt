package com.moodii.app.api

import android.util.Log
import android.view.View
import com.google.gson.GsonBuilder
import com.moodii.app.models.Avatar
import com.moodii.app.models.Mood
import com.moodii.app.models.Mooder
import kotlinx.coroutines.experimental.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Provides api for interacting with moodii service and data
 */

internal interface MoodiiClient {
    @GET("/id/{token}")
    fun getId(@Path("token") token : String): Call<Mooder>
    @GET("/mooders/{uid}")
    fun getMooder(@Path("uid") uid : String): Call<Mooder>
    @GET("/mooders/{uid}/avatar")
    fun getAvatar(@Path("uid") uid : String): Call<Avatar>
    @PUT("/mooders/{uid}/mood")
    fun updateMood(@Path("uid") uid : String, @Body mood: Mood): Call<Mood>
    @PUT("/mooders/{uid}/avatar")
    fun updateAvatar(@Path("uid") uid : String, @Body mood: Avatar): Call<Avatar>
}

object MoodiiApi {
    private var url: String = "http://api.moodii.com:11981"
    private var moodiiClient: MoodiiClient

    init {
        val gson = GsonBuilder().create()

        val retrofit = Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()
        moodiiClient = retrofit.create(MoodiiClient::class.java)
    }

    data class IdResult(var resultCode: Int?, var uid: String?)
    // - verify user on moodii api and returns a UID for moodii client, creates if does not exist.
    // http resultCode 201 for new instance
    fun getId(token: String): IdResult {
        val call = moodiiClient.getId(token)
        val result = async {
            try {call.execute()}
            catch (e:Exception) {return@async null}
        } //co-routines to act like synchronous call
        val response = runBlocking { result.await() } //use Mooder model to get 'id'
        val idResult = IdResult(response?.code(),response?.body()?.id)
        if ((idResult.resultCode == 200 || idResult.resultCode == 201 ) && idResult.uid == null) idResult.resultCode = 500 //should always have uid if response is success 
        return idResult
    }

    fun getMooder(uid: String): Mooder? {
        val call = moodiiClient.getMooder(uid)
        val result = async {
            try {call.execute()}
            catch (e:Exception) {return@async null}
        } //co-routines to act like synchronous call
        val mooder = runBlocking { result.await()?.body() } //return Mooder
        //nts: if we can't get mooder from REST perhaps should load last from local storage. Should inform the user network was unavailable (could overwrite newer!)
        return mooder
    }

    fun getAvatar(uid: String): Avatar? {
        val call = moodiiClient.getAvatar(uid)
        val result = async {
            try {call.execute()}
            catch (e:Exception) {return@async null}
        } //co-routines to act like synchronous call
        val avatar = runBlocking { result.await()?.body() } //return Mooder
        //nts: if we can't get avatar from REST perhaps should load last from local storage. Should inform the user network was unavailable (could overwrite newer!)
        return avatar
    }

    fun updateMood(uid: String, mood: Mood): Boolean {
        val call = moodiiClient.updateMood(uid, mood)
        val result = async {
            try {call.execute()}
            catch (e:Exception) {return@async null}
        }//co-routines to act like synchronous call
        val resultCode = runBlocking() {
            result.await()?.code()
        }
        return (resultCode==200)
    }

    fun updateAvatar(uid: String, mood: Avatar): Boolean {
        val call = moodiiClient.updateAvatar(uid, mood)
        val result = async { try {call.execute()} catch (e:Exception) {return@async null} } //use co-routines to act like synchronous call
        val resultCode = runBlocking { result.await()?.code() }
        return (resultCode==200)
    }


}