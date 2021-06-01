package com.example.guitarapplication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible

/**
 * This fragment represents the first screen of upload song.
 * In this screen (fragment) user enters the name of the song
 * and artist, as well as chooses the mode of the song's information
 * (whether to use tabs or chords)
 */
lateinit var songTitle : String // song's name
lateinit var songArtist : String // song's artist
lateinit var mode : String // flag (whether tabs or chords have been chosen)

class UploadFragment : Fragment() {

    private lateinit var tabs : CheckBox // tabs checkbox
    private lateinit var chords : CheckBox // chords checkbox
    lateinit var next : TextView // next button to send user to the next fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_upload, container, false)

        tabs = view.findViewById(R.id.checkBox1) // tabs checkbox
        chords = view.findViewById(R.id.checkBox2) // chords checkbox
        tabs.setOnClickListener { v -> switchCheckedBox(v) } // set click listener for tabs checkbox
        chords.setOnClickListener { v -> switchCheckedBox(v) } // set click listener for chords checkbox

        next = view.findViewById(R.id.next_tabs) // next button
        next.setOnClickListener { // set click listener for this button
            if (infoSaved(view)) { // if the information about the song has been saved
                if (tabs.isChecked) { // if tabs checkbox was the one checked
                    mode = "___tabs" // set mode to tabs
                    loadTabsUploadFragment() // load tabs upload fragment
                }
                if (chords.isChecked) { // if chords checkbox was the one checked
                    mode = "___chords" // set mode to chords
                    loadChordsUploadFragment() // load chords upload fragment
                }
            }
        }

        return view
    }

    /**
     * Disables a checkbox if another checkbox got checked
     * and sets visibility of Next button (TextView) if at
     * least one of the checkboxes is checked
     */
    private fun switchCheckedBox(v : View) {
        when (v.id) {
            R.id.checkBox1 -> chords.isChecked = false // if tabs are checked, uncheck chords
            R.id.checkBox2 -> tabs.isChecked = false // if chords are checked, uncheck tabs
        }
        // set the next button to visible if one of the checkboxes is checked
        next.isVisible = tabs.isChecked || chords.isChecked
    }

    /**
     * loads TabsUploadFragment
     */
    private fun loadTabsUploadFragment() {
        activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_layout, TabsUploadFragment()).commit()
    }

    /**
     * loads ChordsUploadFragment
     */
    private fun loadChordsUploadFragment() {
        activity!!.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_layout, ChordsUploadFragment()).commit()
    }

    /**
     * Stores the values of song name and song artist
     * and returns true if it was successfully stored
     */
    private fun infoSaved(view : View): Boolean {
        val songTitleView : EditText = view.findViewById(R.id.song_name) // song title view
        val songArtistView : EditText = view.findViewById(R.id.artist_name) // song artist view
        // if the song title and artist are too long/short
        if (songTitleView.text.length < 2 || songTitleView.text.length > 24 ||
            songArtistView.text.length < 2 || songArtistView.text.length > 30) {
            val alert : TextView = view.findViewById(R.id.alert) // alert message
            alert.text = getString(R.string.alert) // display alert message (warning for user)
            return false
        }
        songTitle = songTitleView.text.toString() // save song title
        songArtist = songArtistView.text.toString() // save song artist
        return true
    }
}