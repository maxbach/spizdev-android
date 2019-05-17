package ru.touchin.spizdev.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Completable
import ru.touchin.lifecycle.event.Event
import ru.touchin.spizdev.api.RetrofitController
import ru.touchin.spizdev.logic.Preferences
import ru.touchin.spizdev.models.Phone
import ru.touchin.spizdev.models.SendStampBody
import ru.touchin.spizdev.models.enums.PhoneOs

class MainActivityViewModel : ViewModel() {

    val sendStampProgress = MutableLiveData<Event>()

    fun sendStamp(context: Context) {
        // TODO: add gps and kitkat
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
                    (context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
                    null,
                    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),


                    
                )))
        }
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

}
