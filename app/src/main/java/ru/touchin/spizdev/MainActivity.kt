package ru.touchin.spizdev

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.main_activity_start_service).setOnClickListener {
            ContextCompat.startForegroundService(
                applicationContext,
                Intent(applicationContext, MainService::class.java)
            )
        }
    }
}
