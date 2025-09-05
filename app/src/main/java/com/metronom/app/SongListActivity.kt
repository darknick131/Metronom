package com.metronom.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SongListActivity : AppCompatActivity() {
    
    private lateinit var songManager: SimpleSongManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter
    private lateinit var emptyStateLayout: LinearLayout
    
    companion object {
        const val RESULT_SONG_LOADED = 1001
        const val RESULT_SONG_EDITED = 1002
        const val RESULT_SONG_DELETED = 1003
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list)
        
        songManager = SimpleSongManager(this)
        setupUI()
        loadSongs()
    }
    
    private fun setupUI() {
        recyclerView = findViewById(R.id.recyclerViewSongs)
        emptyStateLayout = findViewById(R.id.layoutEmptyState)
        
        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SongAdapter { song, action ->
            when (action) {
                SongAction.LOAD -> loadSong(song)
                SongAction.EDIT -> editSong(song)
                SongAction.DELETE -> deleteSong(song)
            }
        }
        recyclerView.adapter = adapter
        
        // Setup FAB
        findViewById<FloatingActionButton>(R.id.fabAddSong).setOnClickListener {
            showAddSongDialog()
        }
        
        // Setup back button
        findViewById<MaterialButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
    }
    
    private fun loadSongs() {
        val songs = songManager.getSongs()
        android.util.Log.d("SongListActivity", "Loading ${songs.size} songs")
        
        if (songs.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.updateSongs(songs)
        }
    }
    
    private fun loadSong(song: Song) {
        android.util.Log.d("SongListActivity", "Loading song: ${song.name}")
        val resultIntent = Intent().apply {
            putExtra("song_name", song.name)
            putExtra("song_bpm", song.bpm)
            putExtra("song_latency", song.latency)
        }
        setResult(RESULT_SONG_LOADED, resultIntent)
        finish()
    }
    
    private fun editSong(song: Song) {
        android.util.Log.d("SongListActivity", "Editing song: ${song.name}")
        showEditSongDialog(song)
    }
    
    private fun deleteSong(song: Song) {
        android.util.Log.d("SongListActivity", "Deleting song: ${song.name}")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🗑️ DELETE SONG")
            .setMessage("Are you sure you want to delete '${song.name}'?\n\nBPM: ${song.bpm}${if (song.latency > 0) " | Latency: ${song.latency}ms" else ""}\n\nThis action cannot be undone.")
            .setPositiveButton("🗑️ DELETE SONG") { _, _ ->
                songManager.deleteSong(song)
                Toast.makeText(this, "✅ Song deleted: ${song.name}", Toast.LENGTH_SHORT).show()
                loadSongs() // Refresh the list
            }
            .setNegativeButton("❌ CANCEL", null)
            .show()
    }
    
    private fun showAddSongDialog() {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("➕ ADD NEW SONG")
            .setView(R.layout.dialog_song_edit)
            .setPositiveButton("💾 SAVE SONG") { _, _ -> }
            .setNegativeButton("❌ CANCEL", null)
            .create()
        
        dialog.show()
        
        // Setup dialog content
        val nameInput = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextName)
        val bpmInput = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextBpm)
        val latencyInput = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextLatency)
        
        nameInput?.setText("")
        bpmInput?.setText("120")
        latencyInput?.setText("")
        
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = nameInput?.text?.toString()?.trim() ?: ""
            val bpmText = bpmInput?.text?.toString()?.trim() ?: ""
            val latencyText = latencyInput?.text?.toString()?.trim() ?: ""
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a song name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val bpm = try {
                bpmText.toInt().coerceIn(40, 200)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter a valid BPM (40-200)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val latency = try {
                if (latencyText.isEmpty()) 0 else latencyText.toInt().coerceIn(0, 1000)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter a valid latency (0-1000ms)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            songManager.saveSong(name, bpm, latency)
            val latencyMessage = if (latency > 0) " with ${latency}ms latency" else ""
            Toast.makeText(this, "✅ Song saved: $name$latencyMessage", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            loadSongs() // Refresh the list
        }
    }
    
    private fun showEditSongDialog(song: Song) {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("✏️ EDIT SONG")
            .setView(R.layout.dialog_song_edit)
            .setPositiveButton("💾 SAVE CHANGES") { _, _ -> }
            .setNegativeButton("❌ CANCEL", null)
            .create()
        
        dialog.show()
        
        // Setup dialog content
        val nameInput = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextName)
        val bpmInput = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextBpm)
        val latencyInput = dialog.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editTextLatency)
        
        nameInput?.setText(song.name)
        bpmInput?.setText(song.bpm.toString())
        latencyInput?.setText(if (song.latency > 0) song.latency.toString() else "")
        
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = nameInput?.text?.toString()?.trim() ?: ""
            val bpmText = bpmInput?.text?.toString()?.trim() ?: ""
            val latencyText = latencyInput?.text?.toString()?.trim() ?: ""
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a song name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val bpm = try {
                bpmText.toInt().coerceIn(40, 200)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter a valid BPM (40-200)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val latency = try {
                if (latencyText.isEmpty()) 0 else latencyText.toInt().coerceIn(0, 1000)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Please enter a valid latency (0-1000ms)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Delete old song and save new one
            songManager.deleteSong(song)
            songManager.saveSong(name, bpm, latency)
            
            val latencyMessage = if (latency > 0) " with ${latency}ms latency" else ""
            Toast.makeText(this, "✅ Song updated: $name$latencyMessage", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            loadSongs() // Refresh the list
        }
    }
}

enum class SongAction {
    LOAD, EDIT, DELETE
}

class SongAdapter(
    private val onActionClick: (Song, SongAction) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    
    private var songs = listOf<Song>()
    
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(songs[position])
    }
    
    override fun getItemCount(): Int = songs.size
    
    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songName: TextView = itemView.findViewById(R.id.textSongName)
        private val songDetails: TextView = itemView.findViewById(R.id.textSongDetails)
        private val loadButton: MaterialButton = itemView.findViewById(R.id.buttonLoad)
        private val editButton: MaterialButton = itemView.findViewById(R.id.buttonEdit)
        private val deleteButton: MaterialButton = itemView.findViewById(R.id.buttonDelete)
        
        fun bind(song: Song) {
            songName.text = song.name
            songDetails.text = if (song.latency > 0) {
                "${song.bpm} BPM • ${song.latency}ms latency"
            } else {
                "${song.bpm} BPM"
            }
            
            loadButton.setOnClickListener {
                onActionClick(song, SongAction.LOAD)
            }
            
            editButton.setOnClickListener {
                onActionClick(song, SongAction.EDIT)
            }
            
            deleteButton.setOnClickListener {
                onActionClick(song, SongAction.DELETE)
            }
        }
    }
}
