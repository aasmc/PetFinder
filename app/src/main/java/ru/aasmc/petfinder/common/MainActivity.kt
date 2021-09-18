package ru.aasmc.petfinder.common

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.aasmc.petfinder.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}