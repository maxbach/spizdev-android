package ru.touchin.spizdev.logic

import android.content.Context
import ru.touchin.roboswag.components.utils.storables.PreferenceUtils
import ru.touchin.roboswag.core.observables.storable.NonNullStorable

object Preferences {

    fun getUserHasLogin(context: Context): NonNullStorable<String, Boolean, Boolean> = PreferenceUtils.booleanStorable(
        "LOGIN_HAS_DONE", context.getSharedPreferences(
            "APPLICATION_DATA",
            Context.MODE_MULTI_PROCESS
        ),
        false
    )
}
