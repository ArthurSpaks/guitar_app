package com.example.guitarapplication

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

/**
 * This fragment represents the third screen of upload song.
 * In this screen (fragment) user adds the recording of the song
 * and chooses relevant tags.
 */

var tags = ArrayList<String>()
class SongUploadFragment : Fragment() {

    lateinit var audio : Uri
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_song_upload, container, false)
        val finish : TextView = view.findViewById(R.id.finish)

        val select : Button = view.findViewById(R.id.select_song)
        select.setOnClickListener {
            startFileChooser()
            finish.isVisible = true
        }

        // save a tag and colour for every tag in case it's clicked
        view.findViewById<Button>(R.id.sixties).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.seventies).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.eighties).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.nineties).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.noughties).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.tens).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.acoustic).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.electric).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.bass).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.fingerstyle).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.strum).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.jazz).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.country).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.metal).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.funk).setOnClickListener { (it as Button).saveTag(); it.changeColor() }
        view.findViewById<Button>(R.id.blues).setOnClickListener { (it as Button).saveTag(); it.changeColor() }

        val back : TextView = view.findViewById(R.id.back) // "back" button - sends user back to the previous fragment
        back.setOnClickListener {
            if (mode == "___tabs") loadTabsUploadFragment() // go back to the tabs upload screen
            if (mode == "___chords") loadChordsUploadFragment() // go back to the chords upload screen
            // NOTE: currently this button only returns to the previous screen, however, right now
            // the progress made in it isn't displayed on the screen. In future, the progress should be
            // showed if the user returns to the previous screen, in case they want to change anything
            // (tabs/chords/lyrics)
        }

        finish.setOnClickListener { // when finish button is clicked
            uploadFile() // upload the file to the database
        }

        return view
    }

    /**
     * opens the file explorer of the device for user to choose a song
     */
    private fun startFileChooser() {
        val i = Intent()
        i.type = "audio/mpeg"
        i.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(i, "Select a song"), 1)
    }

    /**
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            audio = data.data!!
            Log.e("media", "onActivityResult: $audio")
        }
    }

    /**
     * upload the file to the database
     */
    private fun uploadFile() {
        val separator = "_!_!_!_!_" // a string that separates the song name and artist
        val pd = ProgressDialog(activity) // a progress dialog for when the song is being uploaded
        pd.setTitle("Uploading") // initial title
        pd.show()

        // create a folder in the database named after song name and author
        val audioRef = FirebaseStorage.getInstance().reference.child("songs/$songTitle$separator$songArtist$mode.mpeg")
        //val audioRef = FirebaseStorage.getInstance().reference.child("$songTitle$separator$songArtist/audiofile.mpeg")
        audioRef.putFile(audio) // upload the audio file to the database
            .addOnSuccessListener { p0 ->// if the file is successfully uploaded
                pd.dismiss()
                Toast.makeText(activity?.applicationContext, "File Uploaded", Toast.LENGTH_LONG).show() // display success message
                Timer("SettingUp", false).schedule(1000) {
                    loadSongsFragment()
                }
            }
            .addOnFailureListener { p0 -> // if the file fails to upload
                pd.dismiss()
                Toast.makeText(activity?.applicationContext, p0.message, Toast.LENGTH_LONG).show() // display error message
            }
            .addOnProgressListener { p0 -> // while the file is being uploaded
                val progress = (100.0 * p0.bytesTransferred) / p0.totalByteCount // calculate upload progress in per cent
                pd.setMessage("Uploaded ${progress.toInt()}%") // display upload progress
            }

        mode = mode.substring(3) // remove the underscores
        val notesRef =
            FirebaseStorage.getInstance().reference.child("$mode/$songTitle$separator$songArtist.txt")
        val lyricsRef =
            FirebaseStorage.getInstance().reference.child("lyrics/$songTitle$separator$songArtist.txt")
        if (mode == "tabs") { // if it was a tabs song
            val tabsAsString = tabsToString(tabs) // get tabs as a string
            notesRef.putBytes(tabsAsString.toByteArray())
            lyricsRef.putBytes(lyricsToString(tabsLyrics).toByteArray()) // write it to the file
        }
        if (mode == "chords") { // if it was a chord song
            val chordsAsString = chordsToString(chords) // get chords as a string
            notesRef.putBytes(chordsAsString.toByteArray())
            lyricsRef.putBytes(lyricsToString(chordsLyrics).toByteArray()) // write it to the file
        }

        val tagsAsString = tagsAsString(tags) // get selected tags as a string
        val tagsRef = FirebaseStorage.getInstance().reference.child("tags/$songTitle$separator$songArtist.txt")
        tagsRef.putBytes(tagsAsString.toByteArray()) // write them to the file

    }

    /**
     *  returns a string representation of the tabs
     */
    private fun tabsToString( tabs : ArrayList<Array<Array<TextView?>>> ): String {
        var result = ""
        for (stringsLayout in tabs) {
            for (step in stringsLayout) {
                for (tab in step) {
                    result += if (tab?.text.isNullOrBlank()) "- " // if the tab is empty, fill it with a - sign
                    else tab?.text?.toString() + " " // else add the tab to it
                }
                result += "\n"
            }
            result += "\n"
        }
        return result
    }

    /**
     *  returns a string representation of the chords
     */
    private fun chordsToString( chords : ArrayList<ArrayList<EditText>> ): String {
        var result = ""
        for (chordLine in chords) {
            for (chord in chordLine) {
                result += if (chord.text.toString() == "") { "- " } // the chord is empty, fill it with a - sign
                else { chord.text.toString() + " " } // else add the chord to it
            }
            result += "\n"
        }
        return result
    }

    /**
     *  returns a string representation of the lyrics
     */
    private fun lyricsToString( lyrics : ArrayList<EditText> ): String {
        var result = ""
        for (line in lyrics) { // for each line
            result += line.text.toString() + "\n" // add the line  of lyrics to the result
        }
        return result
    }

    /**
     * returns a string representation of the tags
     */
    private fun tagsAsString(tags: ArrayList<String>): String {
        var result = ""
        for (tag in tags) { // for every tag
            result += "$tag " // add it to the result
        }
        return result
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
     * loads SongsFragment
     */
    private fun loadSongsFragment() {
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_layout, SongsFragment()).commit()
    }

    /**
     * changes text colour of the button (white -> gold, gold -> white)
     */
    private fun Button.changeColor() {
        when (this.currentTextColor) {
            -1 -> this.setTextColor(contextApp.resources.getColor(R.color.gold)) // change white to gold
            -10496 -> this.setTextColor(contextApp.resources.getColor(R.color.white)) // change gold to white
        }
    }

    /**
     * saves/removes tag to/from the data structure
     */
    private fun Button.saveTag() {
        when (this.currentTextColor) {
            -1 -> tags.add(this.text.toString() + " ") // if white (means gold about to be selected), add the tag
            -10496 -> tags.remove(this.text.toString() + " ") // if gold (means white about to be selected), remove the tag
        }
    }

}