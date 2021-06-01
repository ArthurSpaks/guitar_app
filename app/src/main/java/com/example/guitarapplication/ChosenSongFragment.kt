package com.example.guitarapplication

import android.content.res.Resources
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.storage.FirebaseStorage
import java.io.File

/**
 * This fragment displays the song the user picked.
 * It shows the tabs/chords of the song, lyrics and
 * lets the user play the media file
 */
var startPosY : Int = 0

class ChosenSongTabsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chosen_song, container, false)

        val separator = "_!_!_!_!_" // a string that separates name and artist in the song file
        val title : TextView = view.findViewById(R.id.song_name)
        val artist : TextView = view.findViewById(R.id.artist_name)
        title.measure(0,0)
        artist.measure(0,0)
        startPosY = title.measuredHeight + artist.measuredHeight + 18 // set start vertical position for tabs

        title.text = songName.split("\n")[0] // get song's name
        artist.text = songName.split("\n")[1] // get song's artist
        val fileName = title.text.toString() + separator + artist.text.toString() // build a file name

        val database = FirebaseStorage.getInstance().reference // get reference to database
        val lyricsRef = database.child("lyrics/$fileName.txt") // get reference to the lyrics file of the song
        val localLyricsFile = File.createTempFile(fileName, ".txt") // get its file
        var tabs : ArrayList<Array<Array<String?>>> // a data structure to hold tabs
        var chords : ArrayList<ArrayList<String?>> // a data structure to hold chords
        var lyrics : ArrayList<String> // a data structure to hold lyrics

        if (songMode.substring(3) == "tabs") { // if the song is made out of tabs
            val localTabsFile = File.createTempFile(fileName, ".txt") // create tabs file of the song
            val tabsRef = database.child("tabs/$fileName.txt") // get reference to the tabs file of the song
            tabsRef.getFile(localTabsFile).addOnSuccessListener {
                tabs = localTabsFile.saveTabs() // load tabs to the data structure
                lyricsRef.getFile(localLyricsFile).addOnSuccessListener {
                    lyrics = localLyricsFile.saveLyrics() // load lyrics to the data structure
                    displayTabs(view, tabs, lyrics) // display them on the screen
                }
            }
        }

        if (songMode.substring(3) == "chords") { // if song is made out of chords
            val localChordsFile = File.createTempFile(fileName, ".txt") // create chords file of the song
            val chordsRef = database.child("chords/$fileName.txt") // get reference to the chords file of the song
            chordsRef.getFile(localChordsFile).addOnSuccessListener {
                chords = localChordsFile.saveChords() // load chords to the data structure
                lyricsRef.getFile(localLyricsFile).addOnSuccessListener {
                    lyrics = localLyricsFile.saveLyrics() // load lyrics to the data structure
                    displayChords(view, chords, lyrics) // display them on the screen
                }
            }
        }

        val songRef = database.child("songs/$fileName$songMode.mpeg") // get the media file of the song (the recording of the song)
        songRef.downloadUrl.addOnCompleteListener {
            val url = it.result.toString() // get url to the song in the database
            val mediaPlayer = MediaPlayer().apply { // create a media player
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepare()
            }

            val seekBar : SeekBar = view.findViewById(R.id.seek_bar) // create a seek bar to control the song when it's played
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) { // when the seek bar's thumb is dragged
                    if (fromUser) mediaPlayer.seekTo(progress) // forward the song to the position of the seek bar's thumb
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            seekBar.initialise(mediaPlayer) // initialise the seek bar with the media player
            view.findViewById<ImageButton>(R.id.play).setOnClickListener { // if play button is clicked
                mediaPlayer.start() // start/resume the song
                view.findViewById<ImageButton>(R.id.pause).setOnClickListener { // if pause button is clicked
                    mediaPlayer.pause() // pause the song
                }
            }

        }

        return view
    }

    /**
     * read the tabs from the tabs file and return them in a form of a data structure
     */
    private fun File.saveTabs() : ArrayList<Array<Array<String?>>> {
        val tabs = ArrayList<Array<Array<String?>>>() // data structure that holds tabs
        var line : List<String> // a line in the file
        var stringsLayout = 0 // index of strings layout
        var lineIndex = 0 // index of a line in the strings layout
        tabs.add(Array(16) { arrayOfNulls<String?>(6)})

        this.forEachLine {// iterate through every line in the file (each line has 6 tabs
            // for each guitar string at every position in the song )
            if (it != "") { // if we're on the strings layout (empty line means next strings layout)
                line = it.split(" ") // split the line and write the tabs in
                for (tab in 0..5) { // iterate through the tabs
                    if (line[tab] == "-") { // if the tab is empty (empty tabs are written as dash symbol)
                        tabs[stringsLayout][lineIndex][tab] = null // write null in
                    } else {
                        tabs[stringsLayout][lineIndex][tab] = line[tab] // write tab in
                    }
                }
                lineIndex += 1 // increment line index
                if (lineIndex == 16) { // if the line index is 16 (means last possible position on strings layout)
                    lineIndex = 0 // nullify line index
                    stringsLayout += 1 // increment  strings layout
                    tabs.add(Array(16) { arrayOfNulls<String?>(6) }) // add new array of nulls (next strings layout)
                }
            }
        }
        tabs.removeAt(tabs.size - 1) // remove the excessive array of nulls
        return tabs // return tabs in a form of a data structure
    }

    /**
     * read the chords from the tabs file and return them in a form of a data structure
     */
    private fun File.saveChords() : ArrayList<ArrayList<String?>> {
        val chords = ArrayList<ArrayList<String?>>() // data structure that holds chords
        var line : List<String> // a line in the file
        var i = 0
        this.forEachLine {// iterate through every line in the file (each line has 7 chords)
            chords.add(ArrayList()) // add an empty element to the data structure
            line = it.split(" ") // get list of chords
            for (chord in line) { // for every chord
                if (chord == "-") chords[i].add(null) // if there is no chord (represented by a - sign) add null to the data structure
                else chords[i].add(chord) // else add the chord as a string
            }
            i++
        }
        return chords // return chords in a form of a data structure
    }

    /**
     * display tabs on the screen
     */
    private fun displayTabs(view : View, tabs : ArrayList<Array<Array<String?>>>, lyrics : ArrayList<String>) {
        val linearLayout : LinearLayout = view.findViewById(R.id.linear_layout) // layout with all string layouts
        val relativeLayout : RelativeLayout = view.findViewById(R.id.relative_layout) // layout used for displaying tabs
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        var posY = startPosY // vertical position of tabs
        var layoutsNumber = 0 // number of layouts needed to display the song
        if (tabs.size >= lyrics.size) layoutsNumber = tabs.size else lyrics.size // if there are more lines with
        // lyrics than tabs, set layoutsNumber to lyrics.size, otherwise, tabs.size
        for (stringsLayout in 0 until layoutsNumber) { // iterate through all string layouts
            // get strings layout
            val sLayout : LinearLayout = LayoutInflater.from(contextApp).inflate(R.layout.strings, null) as LinearLayout
            // replace EditView with TextView (if layoutsNumber is less than amount of lyrics' lines)
            if (stringsLayout < lyrics.size) sLayout.replaceLastView(lyrics[stringsLayout])
            else sLayout.replaceLastView(" ") // otherwise replace it with an empty string
            linearLayout.addView(sLayout, linearLayout.childCount) // add strings layout to linear layout
            if (tabs.size >= lyrics.size) {
                sLayout.measure(0, 0)
                val distanceY = sLayout.measuredHeight // distance between string layouts
                val stepX = 20.toPx() // horizontal distance between tabs
                val posX = 40 // horizontal position of tabs
                for (line in 0..15) { // iterate through all 16 possible horizontal positions of tabs
                    for (tab in 0..5) { // iterate through all possible tabs at any horizontal position
                        if (tabs[stringsLayout][line][tab] != null) { // if there is a tab
                            val textView = TextView(contextApp) // create a textView for the tab
                            textView.layoutParams = params // set layout params
                            textView.textSize = 10F // set text size
                            textView.setTextColor(contextApp.resources.getColor(R.color.white)) // set text colour
                            textView.x =
                                (posX + stepX * line).toFloat() // set horizontal position of the tab
                            textView.y =
                                (posY + tab + 32 + (tab) * 25).toFloat() // set vertical position of the tab
                            textView.text = tabs[stringsLayout][line][tab] // set text (tab)
                            relativeLayout.addView(textView) // display the tab on the screen
                        }
                    }
                }
                posY += distanceY // move to the next strings layout
            }
        }
    }

    /**
     * display chords on the screen
     */
    private fun displayChords(view : View, chords : ArrayList<ArrayList<String?>>, lyrics : ArrayList<String>) {
        val linearLayout : LinearLayout = view.findViewById(R.id.linear_layout) // layout with all string layouts
        for ((i, chordsLayout) in chords.withIndex()) { // go through all chordsLayouts
            val cLayout : LinearLayout = LayoutInflater.from(contextApp).inflate(R.layout.chords, null) as LinearLayout // chord Layout (with chords and lyrics)
            val chordLayout : LinearLayout = cLayout.findViewById(R.id.chords_layout) // only chords from the chord Layout
            for (chord in chordsLayout) { // go through every chord in the current chordsLayout
                val chordText = TextView(contextApp)
                chordText.textSize = 12F // set text size
                chordText.setTextColor(contextApp.resources.getColor(R.color.gold)) // set text colour
                chordText.text = chord // set text (chord)
                chordLayout.addView(chordText) // display the tab on the screen
            }
            val lyricsText = TextView(contextApp)
            lyricsText.text = lyrics[i] // set text (lyrics)
            lyricsText.textSize = 12F // set text size
            lyricsText.setTextColor(contextApp.resources.getColor(R.color.white)) // set text colour
            cLayout.removeViewAt(1)
            cLayout.addView(lyricsText, 1)
            cLayout.removeViewAt(2)
            linearLayout.addView(cLayout, linearLayout.childCount)
        }
    }

    /**
     * save lyrics from the file and return them in a form of a data structure
     */
    private fun File.saveLyrics() : ArrayList<String> {
        val lyrics = ArrayList<String>()
        this.forEachLine { lyrics.add(it) }
        return lyrics
    }


    /**
     * replaces EditText with TextView to show lyrics
     */
    private fun LinearLayout.replaceLastView(lyrics : String) {
        val textView = TextView(contextApp) // create a textView for lyrics
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 24, 0, 0)
        textView.layoutParams = params
        textView.text = lyrics
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F) // set text size
        textView.setTextColor(contextApp.resources.getColor(R.color.white)) // set text colour

        this.removeViewAt(this.childCount - 1) // remove editText
        this.addView(textView, this.childCount) // add textView
    }

    /**
     * initialise the seekBar
     */
    private fun SeekBar.initialise(mediaPlayer: MediaPlayer) {
        val seekBar = this
        seekBar.max = mediaPlayer.duration
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    seekBar.progress = mediaPlayer.currentPosition
                    handler.postDelayed(this, 1000) // update the seek bar thumb's position every second
                } catch (e : Exception) {
                    seekBar.progress = 0
                }
            }
        }, 0)
    }

    /**
     * converts density independent pixels to screen pixels
     */
    private fun Int.toPx() : Int = (this * Resources.getSystem().displayMetrics.density).toInt()

}