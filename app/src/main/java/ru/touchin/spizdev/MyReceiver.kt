package ru.touchin.spizdev

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val myIntent = Intent(context, MainService::class.java)
        ContextCompat.startForegroundService(context, myIntent)

    }
}
