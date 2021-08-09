package com.example.guitarapplication

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*
import kotlin.collections.ArrayList

/**
 * This fragment represents the songs that are
 * in the database. It lists all the songs in the
 * database and allows user to pick a song
 */
lateinit var songName : String // song name
lateinit var songMode : String // the mode any song was created in (tabs/chords)
const val separator = "_!_!_!_!_" // separator string that separates the song name and artist

class SongsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_songs, container, false)
        val search : SearchView = view.findViewById(R.id.search) // search bar to search for song through song name/artist
        val tags : ImageButton = view.findViewById(R.id.tags) // image button to choose a tag to limit the available songs
        val linearLayout : LinearLayout = view.findViewById(R.id.linear_layout) // the layout the songs are displayed in

        tags.setOnClickListener { showPopup(tags) } // set click listener for tags button

        val database = FirebaseStorage.getInstance().reference // get reference to the database
        val songsRef = database.child("songs") // reference to the songs folder in the database (mpeg files)
        val songs = songsRef.listAll() // get all files in the folder
        val songList : ArrayList<Button> = ArrayList() // buttons for each song from the database

        songs.addOnCompleteListener { result -> // once all the songs were retrieved
            val items = result.result!!.items
            items.forEach { item ->  // for each song
                item.downloadUrl.addOnCompleteListener { // get url of the song
                    val song = songButton(item) // create a button for this song
                    song.setOnClickListener {// set a click listener for this song's button
                        Log.e("Song name", song.text.toString()) //
                        songName = song.text.toString() // save the song name
                        songMode = getSongMode(item) // save the song mode
                        loadChosedSongTabsFragment() // load the chosen song fragment and display the song information
                    }
                    songList.add(song) // add the button to the list
                    songList.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.text.toString() }) // sort the list
                    linearLayout.addView(song, songList.indexOf(song)) // display the button on the screen at a sorted position
                }
            }
            Log.e("songList", songList.toString())
            /*
            songList.sortWith(Comparator { b1: Button, b2: Button ->
                b1.text.toString().compareTo(b2.text.toString())
            })*/

        }// .addOnSuccessListener { for (song in songList) linearLayout.addView(song) }

        return view
    }

    /**
     * retrieve the song mode ("___tabs"/"___chords") from the file name
     */
    private fun getSongMode(item : StorageReference) : String {
        return "___${item.name.split(separator)[1].split("___")[1].substring(0,
            item.name.split(separator)[1].split("___")[1].length - 5)}"
    }

    /**
     * handle the tags button and show the available tags that songs
     * can have
     */
    private fun showPopup(view : View) {
        val popup = PopupMenu(activity, view)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.top_menu, popup.menu) // inflate the popup for the menu
        popup.setOnMenuItemClickListener { menuItem -> // set click listener for each tag item
            when(menuItem.itemId){
                R.id.sixties-> { // if 60's clicked
                    Toast.makeText(activity?.applicationContext, "sixties clicked", Toast.LENGTH_LONG).show()
                }
                R.id.seventies-> { // 70's clicked
                    Toast.makeText(activity?.applicationContext, "seventies clicked", Toast.LENGTH_LONG).show()
                }
                R.id.eighties-> { // if 80's clicked
                    Toast.makeText(activity?.applicationContext, "eighties clicked", Toast.LENGTH_LONG).show()
                }
                R.id.nineties-> { // if 90's clicked
                    Toast.makeText(activity?.applicationContext, "nineties clicked", Toast.LENGTH_LONG).show()
                }
                R.id.noughties-> { // if 00's clicked
                    Toast.makeText(activity?.applicationContext, "noughties clicked", Toast.LENGTH_LONG).show()
                }
                R.id.tens-> { // if 10's clicked
                    Toast.makeText(activity?.applicationContext, "tens clicked", Toast.LENGTH_LONG).show()
                }
                R.id.acoustic-> { // if Acoustic clicked
                    Toast.makeText(activity?.applicationContext, "acoustic clicked", Toast.LENGTH_LONG).show()
                }
                R.id.electric-> { // if Electric clicked
                    Toast.makeText(activity?.applicationContext, "electric clicked", Toast.LENGTH_LONG).show()
                }
                R.id.bass-> { // if Bass clicked
                    Toast.makeText(activity?.applicationContext, "bass clicked", Toast.LENGTH_LONG).show()
                }
                R.id.fingerstyle-> { // if Fingerstyle clicked
                    Toast.makeText(activity?.applicationContext, "fingerstyle clicked", Toast.LENGTH_LONG).show()
                }
                R.id.strum-> { // if Strum clicked
                    Toast.makeText(activity?.applicationContext, "strum clicked", Toast.LENGTH_LONG).show()
                }
                R.id.jazz-> { // if Jazz clicked
                    Toast.makeText(activity?.applicationContext, "jazz clicked", Toast.LENGTH_LONG).show()
                }
                R.id.country-> { // if Country clicked
                    Toast.makeText(activity?.applicationContext, "country clicked", Toast.LENGTH_LONG).show()
                }
                R.id.metal-> { // if Metal clicked
                    Toast.makeText(activity?.applicationContext, "metal clicked", Toast.LENGTH_LONG).show()
                }
                R.id.funk-> { // if Funk clicked
                    Toast.makeText(activity?.applicationContext, "funk clicked", Toast.LENGTH_LONG).show()
                }
                R.id.blues-> { // if Blues clicked
                    Toast.makeText(activity?.applicationContext, "blues clicked", Toast.LENGTH_LONG).show()
                }

            }
            true
        }
        popup.show() // display the popup
    }

    /**
     * set up song button's style
     */
    private fun songButton(item: StorageReference): Button {
        val button = Button(contextApp) // create button view
        button.tag = item.name.split(separator)[0] + item.name.split(separator)[1]
        val modeName = item.name.split(separator)[1].split("___")[1] // get mode (tags/chords)
        val params = LinearLayout.LayoutParams( // set layout parameters for teh button view
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 0, 0, 0) // set margins
        button.layoutParams = params // apply the layout parameters to the button
        button.isAllCaps = false // disable default all caps setting for button's text
        button.textAlignment = View.TEXT_ALIGNMENT_VIEW_START // align button's text to the left
        // create the text (white and big text for song's name, golden and small text for artist)
        val text = "<font color=#" + Integer.toHexString(ContextCompat.getColor(
            contextApp, R.color.white) and 0x00ffffff) + ">" + "<big>" + item.name.split(separator)[0] + "</big>" + "<br />" + "</font> " +
                "<font color=#" + Integer.toHexString(ContextCompat.getColor(
            contextApp, R.color.gold) and 0x00ffffff) + ">" + "<small>" +
                item.name.split(separator)[1].substring(0, item.name.split(separator)[1].length - modeName.length - 3) + "</small>" + "</font>"
        button.text = Html.fromHtml(text) // apply the text to the button
        button.setBackgroundColor(contextApp.resources.getColor(R.color.transparent)) // set background transparent
        return button
    }

    /**
     * loads ChosenSongTabsFragment
     */
    private fun loadChosedSongTabsFragment() {
        activity!!.supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_layout, ChosenSongTabsFragment()).commit()
    }
}