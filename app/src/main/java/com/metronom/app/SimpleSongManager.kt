package com.metronom.app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

class SimpleSongManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("songs", Context.MODE_PRIVATE)
    private val songsKey = "saved_songs"
    
    fun saveSong(name: String, bpm: Int, latency: Int = 0) {
        val songs = getSongs().toMutableList()
        songs.add(Song(name, bpm, latency))
        saveSongs(songs)
    }
    
    fun getSongs(): List<Song> {
        val jsonString = prefs.getString(songsKey, "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val songs = mutableListOf<Song>()
        
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            songs.add(Song(
                name = jsonObject.getString("name"),
                bpm = jsonObject.getInt("bpm"),
                latency = jsonObject.optInt("latency", 0)
            ))
        }
        
        return songs
    }
    
    fun deleteSong(song: Song) {
        val songs = getSongs().toMutableList()
        songs.removeAll { it.name == song.name && it.bpm == song.bpm && it.latency == song.latency }
        saveSongs(songs)
    }
    
    private fun saveSongs(songs: List<Song>) {
        val jsonArray = JSONArray()
        songs.forEach { song ->
            val jsonObject = JSONObject()
            jsonObject.put("name", song.name)
            jsonObject.put("bpm", song.bpm)
            jsonObject.put("latency", song.latency)
            jsonArray.put(jsonObject)
        }
        // Use commit() instead of apply() to ensure synchronous persistence
        val success = prefs.edit().putString(songsKey, jsonArray.toString()).commit()
        android.util.Log.d("SimpleSongManager", "Songs saved: $success, count: ${songs.size}")
    }
}

data class Song(
    val name: String,
    val bpm: Int,
    val latency: Int = 0
)
