package ru.touchin.spizdev.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.LocationRequest
import io.reactivex.Completable
import ru.touchin.lifecycle.event.Event
import ru.touchin.lifecycle.viewmodel.RxViewModel
import ru.touchin.livedata.location.LocationLiveData
import ru.touchin.spizdev.MainActivity
import ru.touchin.spizdev.api.RetrofitController
import ru.touchin.spizdev.logic.Preferences
import ru.touchin.spizdev.models.GpsPosition
import ru.touchin.spizdev.models.Phone
import ru.touchin.spizdev.models.SendStampBody
import ru.touchin.spizdev.models.enums.PhoneOs

class MainActivityViewModel : RxViewModel() {

    private lateinit var context: Context
    val sendStampProgress = MutableLiveData<Event>()
    private val liveDataLocation by lazy { LocationLiveData(context, LocationRequest()) }
    private val wiFiLiveData by lazy { WiFiLiveData(context) }

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    fun observeLiveData(activity: MainActivity) {
        this.context = activity
        liveDataLocation.observe(activity, Observer { })
        wiFiLiveData.observe(activity, Observer { })
    }

    @SuppressLint("HardwareIds")
    fun sendStamp() {
        // TODO: battery kitkat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Preferences.getUserHasLogin(context)
                    .get()
                    .flatMapCompletable { userHasLogin ->
                        if (!userHasLogin) {
                            login(context)
                                    .andThen(Preferences.getUserHasLogin(context).set(true))
                        } else {
                            Completable.complete()
                        }
                    }
                    .andThen(RetrofitController.api.sendStamp(SendStampBody(
                            batteryLevel = getBatteryLevel(),
                            gpsPosition = liveDataLocation.value?.let { GpsPosition(it.latitude, it.longitude, it.accuracy) },
                            phoneId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
                            wiFiScans = wiFiLiveData.value.orEmpty()
                    )))
                    .dispatchTo(sendStampProgress)
        }
    }

    @SuppressLint("HardwareIds")
    private fun login(context: Context) = RetrofitController.api.login(
        Phone(
            id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            os = PhoneOs.ANDROID,
            osVersion = Build.VERSION.RELEASE
        )
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getBatteryLevel() = (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager)
            .getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

}
