package com.example.guitarapplication

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment

/**
 * This fragment represents the second screen of upload song (that uses tabs).
 * In this screen (fragment) user enters the tabs of the song
 * and lyrics
 */

val tabs = ArrayList<Array<Array<TextView?>>>() // main data structure for keeping tabs' data.
// A 3d structure that can hold a tab for each guitar string on every possible horizontal step on every block of guitar strings (track).
// Used as a source of information to pass to the database
val tabsLyrics = ArrayList<EditText>() // main data structure for keeping data lyrics' data. Used as a source of information to pass to the database
var stepVertical = 1 // current vertical position of the pointer (which stringsLayout is it on at the moment)
var stepHorizontal = 1 // current horizontal position of the pointer at the current stringsLayout (1..16, because it can be at 16 positions maximum)

class TabsUploadFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_tabs_upload, container, false)

        val title : TextView = view.findViewById(R.id.song_name) //
        val artist : TextView = view.findViewById(R.id.artist_name)
        title.text = songTitle
        artist.text = songArtist

        val linearLayout : LinearLayout = view.findViewById(R.id.linear_layout)
        val stringsLayout : LinearLayout =
            LayoutInflater.from(contextApp).inflate(R.layout.strings, null) as LinearLayout // get stringsLayout
        // (block of guitar strings and and EditText for lyrics)
        tabsLyrics.add(stringsLayout.getChildAt(stringsLayout.childCount - 1) as EditText) // add lyrics' textview to the data structure
        linearLayout.addView(stringsLayout, linearLayout.childCount - 1) // add it to the screen above the buttons

        stringsLayout.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)  // measure the stringsLayout
        val distanceY = stringsLayout.measuredHeight + 18 // get layout's height plus margins to move the pointer up/down the guitar strings
        //Log.e("height", distanceY.toString())

        val pos : ImageView = view.findViewById(R.id.position) // pointer that points at the current position where tabs can be added
        val params = RelativeLayout.LayoutParams (
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        title.measure(0,0)
        artist.measure(0, 0)
        params.setMargins(20, title.measuredHeight + artist.measuredHeight + 18, 0, 0)
        pos.layoutParams = params
        val left : Button = view.findViewById(R.id.move_left) // button used to move pointer to the left
        val right : Button = view.findViewById(R.id.move_right) // button used to move pointer to the right
        val relativeLayout : RelativeLayout = view.findViewById(R.id.relative_layout) // layout used to place tabs on the screen

        val startPosX = pos.x + pos.marginStart // leftmost possible X position of pointer
        val stepX = 20.toPx() //1
        // step that pointer moves horizontally+
        val endPosX = startPosX + 15 * stepX //806.0F // rightmost possible X position of pointer

        left.setOnClickListener {
            if (pos.x > startPosX) {  // if the pointer is supposed to move left
                stepHorizontal -= 1 // decrement stepHorizontal as the pointer is moved 1 step to the left
                pos.x -= stepX // move the pointer to the left on the screen
            } else if (stepVertical > 1) { // if the pointer is supposed to move up to the rightmost position
                stepVertical -= 1 // decrement stepVertical as the pointer is moved up to the previous strings layout
                stepHorizontal = 16 // set stepHorizontal to 16 as the pointer is moved to the rightmost position
                pos.x = endPosX // move the pointer to the rightmost position on the screen
                pos.y -= distanceY // move the pointer up to the previous strings layout
            }
        }

        right.setOnClickListener {
            if (pos.x < endPosX) { // if the pointer is supposed to move right
                stepHorizontal += 1 // increment stepHorizontal as the pointer is moved 1 step to the right
                pos.x += stepX // move pointer to the right on the screen
            } else if (stepVertical + 3 < linearLayout.childCount) { // if the pointer is supposed to move down to the leftmost position
                stepVertical += 1 // increment stepVertical as the pointer is moved down to the next strings layout
                stepHorizontal = 1 // set stepHorizontal to 1 as the pointer is moved to the leftmost position
                pos.x = startPosX // move the pointer to the leftmost position on the screen
                pos.y += distanceY // move the pointer down to the next strings layout
            }
        }

        val tabsAdd : View = view.findViewById(R.id.add_line) // button to add new strings layout at the bottom of the previous ones
        tabsAdd.setOnClickListener {
            val stringsLayout : LinearLayout =
                LayoutInflater.from(contextApp).inflate(R.layout.strings, null) as LinearLayout // get the strings layout
            tabs.add(Array(16) { arrayOfNulls<TextView>(6)}) // add a new guitar strings layout as a 2d array
            tabsLyrics.add(stringsLayout.getChildAt(stringsLayout.childCount - 1) as EditText) // add edittext (it's the last view in strings layout)
            linearLayout.addView(stringsLayout, linearLayout.childCount - 1) // add the strings layout to the screen
        }

        val tabsRemove : View = view.findViewById(R.id.remove_line) // button to remove the last strings layout
        tabsRemove.setOnClickListener {
            if (linearLayout.childCount > 4) { // if there is more than one strings' layout
                for (horizontal_pos in 1..16) { // go through all displayed tabs
                    for (string in 1..6) {  // on the last strings' layout
                        if (tabs[linearLayout.childCount - 3 - 1][horizontal_pos - 1][string - 1] != null) { // if there is a tab displayed
                            // remove it (remove all tabs displayed on the last strings' layout (the one about to be deleted))
                            relativeLayout.removeView(tabs[linearLayout.childCount - 3 - 1][horizontal_pos - 1][string - 1])
                        }
                    }
                }
                if (stepVertical == linearLayout.childCount - 3) { // if the pointer is on the strings that are being deleted
                    pos.x = endPosX // move pointer's X to the rightmost position
                    pos.y -= distanceY // move pointer up to the previous strings
                    stepHorizontal = 16 // set stepHorizontal's value to 16 (maximum)
                    stepVertical -= 1 // decrement stepVertical's value by 1 (the pointer was moved back up on the previous strings)
                }
                tabs.removeAt(stepVertical) // remove current strings layout from the data structure
                tabsLyrics.removeAt(tabsLyrics.size - 1)
                linearLayout.removeViewAt(linearLayout.childCount - 2) // remove the strings layout
            }
        }

        val delete : Button = view.findViewById(R.id.delete_tab) // button for deleting the tabs the pointer points at
        delete.setOnClickListener {
            for (i in 1..6) {  // go through all 6 guitar strings
                if (tabs[stepVertical - 1][stepHorizontal - 1][i - 1] != null) { // if current string already has a tab set
                    relativeLayout.removeView(tabs[stepVertical - 1][stepHorizontal - 1][i - 1]) // remove it from the screen
                    tabs[stepVertical - 1][stepHorizontal - 1][i - 1] = null // remove it from the data structure
                }
            }
        }

        tabs.add(Array(16) { arrayOfNulls<TextView>(6)}) // add first strings layout to the data structure (empty)
        setTabsKeyboard(view, pos, relativeLayout) // set tabs keyboard

        val back : TextView = view.findViewById(R.id.back_tabs) // "back" button - sends user back to the previous fragment
        back.setOnClickListener {
            loadUploadFragment()
        }

        val next : TextView = view.findViewById(R.id.next_tabs) // "next" button - sends user to the next fragment where they upload the music file
        next.setOnClickListener {
            loadSongUploadFragment()
        }

        return view
    }

    /**
     * converts density independent pixels to screen pixels
     */
    private fun Int.toPx() : Int = (this * Resources.getSystem().displayMetrics.density).toInt()

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

    /**
     * sets tabs keyboard. Adds onclick listeners to each tab button on the keyboard
     * and displays the tab on the screen accordingly, as well as adds it to the
     * data structure
     */
    private fun setTabsKeyboard(view : View, pointer : ImageView, relativeLayout : RelativeLayout) {
        for (i in 1..6) { // go through all rows of tabs on the keyboard (each row corresponds to one guitar string)
            for (j in 0..21) { // go through all columns of tabs on the keyboard (each column corresponds to one guitar tab)
                val button : Button = view.findViewWithTag(i.toString() + j.toString()) // find every button on the keyboard
                button.setOnClickListener {
                    val tab = TextView(activity) // create a textview to display this tab
                    tab.text = button.tag.toString().substring(1) // tag (except for the first symbol) has the same text as button's text
                    tab.setTextColor(resources.getColor(R.color.white)) // set tab's colour as white
                    tab.textSize = 10F // set text size
                    tab.x = pointer.x + pointer.width / 2 - 5 // place the tab at the middle of the pointer horizontally
                    tab.y = pointer.y + 32 + (i-1) * 25 // place the tab vertically at one of the guitar strings
                    //Log.e("steps", "$stepVertical $stepHorizontal $i")
                    if (tabs[stepVertical-1][stepHorizontal-1][i-1] != null) { // if there has already been a tab set at this position
                        relativeLayout.removeView(tabs[stepVertical-1][stepHorizontal-1][i-1]) // remove it
                    }
                    tabs[stepVertical-1][stepHorizontal-1][i-1] = tab // add the tab to the data structure
                    relativeLayout.addView(tabs[stepVertical-1][stepHorizontal-1][i-1]) // display the tab on the screen
                }
            }
        }
    }
}
