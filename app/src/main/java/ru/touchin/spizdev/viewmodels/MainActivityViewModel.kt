package ru.touchin.spizdev.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationRequest
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import ru.touchin.lifecycle.event.Event
import ru.touchin.lifecycle.viewmodel.BaseDestroyable
import ru.touchin.lifecycle.viewmodel.Destroyable
import ru.touchin.lifecycle.viewmodel.LiveDataDispatcher
import ru.touchin.livedata.location.LocationLiveData
import ru.touchin.spizdev.api.RetrofitController
import ru.touchin.spizdev.models.GpsPosition
import ru.touchin.spizdev.models.Phone
import ru.touchin.spizdev.models.SendStampBody
import ru.touchin.spizdev.models.WiFiScan
import ru.touchin.spizdev.models.enums.PhoneOs

class MainActivityViewModel(
    private val destroyable: BaseDestroyable = BaseDestroyable(),
    private val liveDataDispatcher: AsyncLiveDataDispatcher = AsyncLiveDataDispatcher(destroyable)
) : ViewModel(), Destroyable by destroyable, LiveDataDispatcher by liveDataDispatcher {

    private lateinit var context: Context

    val sendStampProgress = MutableLiveData<Event>()
    private val liveDataLocation by lazy {
        LocationLiveData(
            context, LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(30000)
        )
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun observeLiveData(context: Context, owner: LifecycleOwner) {
        this.context = context.applicationContext
        liveDataLocation.observe(owner, Observer { })
    }

    fun removeObservers(owner: LifecycleOwner) {
        liveDataLocation.removeObservers(owner)
    }

    @SuppressLint("HardwareIds")
    fun sendStamp() {
        // TODO: battery kitkat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sendStampCompletable()
                .dispatchTo(sendStampProgress)
        }
    }

    fun onDestroy() {
        destroyable.onDestroy()
    }

    @SuppressLint("HardwareIds")
    private fun login() = RetrofitController.api.login(
        Phone(
            id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            os = PhoneOs.ANDROID,
            osVersion = Build.VERSION.RELEASE
        )
    )

    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun sendStampCompletable(): Completable = getWiFiScan().flatMapCompletable { wiFiScan ->
        RetrofitController.api.sendStamp(
            SendStampBody(
                batteryLevel = getBatteryLevel(),
                gpsPosition = liveDataLocation.value?.let {
                    GpsPosition(
                        it.latitude,
                        it.longitude,
                        it.accuracy
                    )
                },
                phoneId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
                wiFiScans = wiFiScan
            )
        )
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext { throwable ->
                if (throwable is HttpException && throwable.code() == 401) {
                    login()
                        .andThen(sendStampCompletable())
                } else {
                    Completable.error(throwable)
                }
            }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getBatteryLevel() = (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager)
        .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    private fun getWiFiScan(): Single<List<WiFiScan>> = Single.create { emitter: SingleEmitter<List<WiFiScan>> ->
        val wiFiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wiFiManager.startScan()
        val filter = IntentFilter()
        filter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE")
        filter.addAction("android.net.wifi.STATE_CHANGE")
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                emitter.onSuccess(wiFiManager.scanResults.map { scanResult ->
                    WiFiScan(
                        scanResult.SSID,
                        scanResult.BSSID,
                        scanResult.frequency,
                        scanResult.level
                    )
                })
                context.unregisterReceiver(this)
            }
        }
        context.registerReceiver(broadcastReceiver, filter)
    }

}
