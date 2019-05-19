package ru.touchin.spizdev.viewmodels

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.lifecycle.LiveData
import ru.touchin.spizdev.models.WiFiScan

class WiFiLiveData(private val context: Context) : LiveData<List<WiFiScan>>() {

    private var broadcastReceiver: BroadcastReceiver? = null

    private fun prepareReceiver() {
        val filter = IntentFilter()
        filter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE")
        filter.addAction("android.net.wifi.STATE_CHANGE")
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val wifiMgr = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                postValue(wifiMgr.scanResults.map { scanResult -> WiFiScan(
                        scanResult.SSID,
                        scanResult.BSSID,
                        scanResult.frequency,
                        scanResult.level
                ) })

            }
        }
        context.registerReceiver(broadcastReceiver, filter)
    }

    override fun onActive() {
        prepareReceiver()
    }

    override fun onInactive() {
        context.unregisterReceiver(broadcastReceiver)
        broadcastReceiver = null
    }

}
