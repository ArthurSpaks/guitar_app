package com.example.guitarapplication

import android.os.Bundle
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children

/**
 * This fragment represents the second screen of upload song (that uses lyrics).
 * In this screen (fragment) user enters the chords of the song
 * and lyrics
 */

val chords = ArrayList<ArrayList<EditText>>()
val chordsLyrics = ArrayList<EditText>()

class ChordsUploadFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chords_upload, container, false)

        val title : TextView = view.findViewById(R.id.song_name)
        val artist : TextView = view.findViewById(R.id.artist_name)
        title.text = songTitle
        artist.text = songArtist

        val linearLayout : LinearLayout = view.findViewById(R.id.linear_layout)
        val allChordLayouts : ArrayList<View> = ArrayList() // all chord layouts (chords and lyrics)

        allChordLayouts.add(LayoutInflater.from(contextApp).inflate(R.layout.chords, null))
        linearLayout.addView(allChordLayouts[0], linearLayout.childCount - 1)
        allChordLayouts[0].findViewById<LinearLayout>(R.id.chords_layout).createChord()

        allChordLayouts[0].findViewById<ImageButton>(R.id.add_chord).setOnClickListener {
            val chords = allChordLayouts[0].findViewById<LinearLayout>(R.id.chords_layout)
            if (chords.childCount < 7) {
                chords.createChord()
            }
        }

        allChordLayouts[0].findViewById<ImageButton>(R.id.remove_chord).setOnClickListener {
            val chords = allChordLayouts[0].findViewById<LinearLayout>(R.id.chords_layout)
            if (chords.childCount > 1) chords.removeViewAt(chords.childCount - 1)
        }

        val chordsLayoutAdd : View = view.findViewById(R.id.add_line)
        chordsLayoutAdd.setOnClickListener {
            allChordLayouts.add(LayoutInflater.from(contextApp).inflate(R.layout.chords, null))
            val index = allChordLayouts.size - 1
            linearLayout.addView(allChordLayouts[index], linearLayout.childCount - 1)
            allChordLayouts[index].findViewById<LinearLayout>(R.id.chords_layout).createChord()
            allChordLayouts[index].findViewById<ImageButton>(R.id.add_chord).setOnClickListener {
                val chords = allChordLayouts[index].findViewById<LinearLayout>(R.id.chords_layout)
                if (chords.childCount < 7) {
                    chords.createChord()
                }
            }
            allChordLayouts[index].findViewById<ImageButton>(R.id.remove_chord).setOnClickListener {
                val chords = allChordLayouts[index].findViewById<LinearLayout>(R.id.chords_layout)
                if (chords.childCount > 1) chords.removeViewAt(chords.childCount - 1)
            }
        }

        val chordsRemove : View = view.findViewById(R.id.remove_line)
        chordsRemove.setOnClickListener {
            if (linearLayout.childCount > 4) {
                allChordLayouts.removeAt(allChordLayouts.size - 1)
                linearLayout.removeViewAt(linearLayout.childCount - 2)
            }
        }

        val back : TextView = view.findViewById(R.id.back_chords)
        back.setOnClickListener {
            loadUploadFragment()
        }

        val next : TextView = view.findViewById(R.id.next_chords)
        next.setOnClickListener {
            saveInfo(allChordLayouts)
            loadSongUploadFragment()
        }

        return view
    }

    private fun TextView.setMaxLength(length : Int) {
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(length)
        this.filters = filterArray
    }

    /**
     * create a chord editText and load it to the linear layout
     */
    private fun LinearLayout.createChord() {
        val chord = EditText(contextApp)
        chord.setTextColor(contextApp.resources.getColor(R.color.gold))
        chord.setMaxLength(5)
        chord.textSize = 12F
        chord.background = null
        chord.hint = getString(R.string.chord)
        this.addView(chord, this.childCount)
    }

    /**
     * loads UploadFragment
     */
    private fun loadUploadFragment() {
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_layout, UploadFragment()).commit()
    }

    /**
     * loads SongUploadFragment
     */
    private fun loadSongUploadFragment() {
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_layout, SongUploadFragment()).commit()
    }

    private fun saveInfo(allChordsLayout : ArrayList<View>) {
        for ((i, layout) in allChordsLayout.withIndex()) {
            chords.add(ArrayList())
            for (chord in layout.findViewById<LinearLayout>(R.id.chords_layout).children)
                chords[i].add(chord as EditText)
            chordsLyrics.add(layout.findViewById(R.id.lyrics))
        }
    }

}