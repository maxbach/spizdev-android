package ru.touchin.spizdev.api

import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.POST
import ru.touchin.spizdev.models.Phone
import ru.touchin.spizdev.models.SendStampBody

interface ServerApi {

    @POST("/mobile/login")
    fun login(@Body body: Phone): Completable

    @POST("/mobile/stamp")
    fun sendStamp(@Body body: SendStampBody): Completable

}
