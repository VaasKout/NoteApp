package com.example.noteexample.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.noteexample.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /**
     * Application is written on MVVM architecture
     * with single activity
     *
     * @see R.navigation.navigation
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}