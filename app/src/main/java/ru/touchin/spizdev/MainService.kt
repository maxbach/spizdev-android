package ru.touchin.spizdev

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.permissioneverywhere.PermissionEverywhere
import ru.touchin.lifecycle.viewmodel.LifecycleViewModelProviders
import ru.touchin.spizdev.viewmodels.MainActivityViewModel
import java.util.Timer
import java.util.TimerTask

class MainService : LifecycleService() {

    companion object {
        private const val REQ_CODE: Int = 1
        private const val NOTIFICATION_CODE: Int = 2
        private const val NOTIFICATION_CHANNEL_CODE: String = "SpizdevNotificationChannerl"
        private const val TIMER_DELAY_IN_MINUTES = 1L
        private const val TIMER_INTERVAL_IN_MINUTES = 5L
    }

    private val viewModel by lazy { LifecycleViewModelProviders.of(this).get(MainActivityViewModel::class.java) }
    private val timer = Timer()

    override fun onBind(intent: Intent?): IBinder? {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            createNotificationWithPermissionRequest()
        } else {
            observeAndStartTimer()
        }
        return super.onBind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            startForeground(NOTIFICATION_CODE, NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_CODE)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Touchin Device Manager")
                    .setContentText("Следим за тобой и за тем, где находится девайс")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build());
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
        timer.cancel()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private fun createNotificationWithPermissionRequest() {
        PermissionEverywhere.getPermission(applicationContext,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQ_CODE,
                "PERMISSSIONS",
                "This app needs a location permission",
                R.mipmap.ic_launcher
        )
                .enqueue { permissionResponse ->
                    if (permissionResponse.isGranted) {
                        observeAndStartTimer()
                    } else {
                        createNotificationWithPermissionRequest()
                    }
                }
    }

    @SuppressLint("MissingSuperCall")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    // for kitkat
    override fun onTaskRemoved(rootIntent: Intent) {
        val restartService = Intent(applicationContext,
                this.javaClass)
        restartService.setPackage(packageName)
        val restartServicePI = PendingIntent.getService(
                applicationContext, 1, restartService,
                PendingIntent.FLAG_ONE_SHOT)

        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 100, restartServicePI)

    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    private fun observeAndStartTimer() {
        viewModel.observeLiveData(this)
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // TODO add check to network, gps and wifi
                viewModel.sendStamp()
            }
        }, TIMER_DELAY_IN_MINUTES * 1000 * 60, TIMER_INTERVAL_IN_MINUTES * 60 * 1000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NOTIFICATION_CHANNEL_CODE
            val descriptionText = NOTIFICATION_CHANNEL_CODE
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_CODE, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
