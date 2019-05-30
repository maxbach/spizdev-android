package ru.touchin.spizdev

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import ru.touchin.spizdev.viewmodels.MainActivityViewModel

class MainActivity : AppCompatActivity() {

    private val snackbar by lazy { Snackbar.make(findViewById(R.id.main_activity_parent), "Test", Snackbar.LENGTH_SHORT) }

    private val viewModel by lazy { MainActivityViewModel() }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.main_activity_start_service).setOnClickListener {
            ContextCompat.startForegroundService(
                applicationContext,
                Intent(applicationContext, MainService::class.java)
            )
        }
        viewModel.observeLiveData(this, this)
        viewModel.sendStampProgress.observe(this, Observer {
            snackbar.setText(it.toString())
            snackbar.show()
        })
        findViewById<View>(R.id.button_send_stamp).setOnClickListener {
            viewModel.sendStamp()
        }
    }
}
