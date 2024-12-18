package com.example.accountbooks.extensions

import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.accountbooks.R

fun AppCompatActivity.setupToolbar(title: String) {
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayShowTitleEnabled(false)
    
    toolbar.findViewById<TextView>(R.id.tvTitle).text = title
    
    toolbar.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
        finish()
    }
    
    toolbar.findViewById<ImageButton>(R.id.btnHome).setOnClickListener {
        finish()
    }
} 