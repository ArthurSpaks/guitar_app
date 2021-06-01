package com.example.guitarapplication

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

lateinit var contextApp : Context
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        contextApp = this

        // set up window settings
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED // lock the screen orientation
        supportActionBar?.hide() // hide the action bar (the top bar)

        // set up Bottom Navigation
        val btnNav : BottomNavigationView = findViewById(R.id.bottom_navigation_view)
        btnNav.setOnNavigationItemSelectedListener { item -> switchItems(item) }

        // set selected tab by default (songs)
        btnNav.selectedItemId = R.id.songs
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_layout, SongsFragment()).commit()

    }

    /**
     * switches between fragment when user clicks
     * on the bottom navigation bar items
     */
    private fun switchItems(item : MenuItem): Boolean {
        var selectedFragment : Fragment? = null
        when (item.itemId) {
            R.id.information -> selectedFragment = InformationFragment() // switch to InformationFragment
            R.id.songs -> selectedFragment = SongsFragment() // switch to SongsFragment
            R.id.upload -> selectedFragment = UploadFragment() // switch to UploadFragment
        }

        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_layout, selectedFragment).commit()
        }
        return true
    }

}