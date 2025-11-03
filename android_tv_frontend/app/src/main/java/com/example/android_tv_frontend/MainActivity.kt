package com.example.android_tv_frontend

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.view.KeyEvent
import com.example.android_tv_frontend.ui.browse.BrowseFragment

/**
 * Main Activity for Android TV Recipe Guide.
 * Extends FragmentActivity for Leanback compatibility and hosts the browse and detail fragments.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Load browse fragment on startup
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, BrowseFragment.newInstance())
                .commit()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle TV remote control inputs
        return when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER,
            KeyEvent.KEYCODE_ENTER -> {
                // Handle SELECT/OK button - let fragments handle this
                super.onKeyDown(keyCode, event)
            }
            KeyEvent.KEYCODE_BACK -> {
                // Handle BACK button - pop back stack or exit
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                    true
                } else {
                    finish()
                    true
                }
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
