package ru.touchin.spizdev.viewmodels

import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import ru.touchin.lifecycle.event.ContentEvent
import ru.touchin.lifecycle.event.Event
import ru.touchin.lifecycle.viewmodel.BaseDestroyable
import ru.touchin.lifecycle.viewmodel.Destroyable
import ru.touchin.lifecycle.viewmodel.LiveDataDispatcher

class AsyncLiveDataDispatcher(private val destroyable: BaseDestroyable = BaseDestroyable()) : LiveDataDispatcher,
    Destroyable by destroyable {

    override fun <T> Flowable<out T>.dispatchTo(liveData: MutableLiveData<ContentEvent<T>>): Disposable {
        liveData.postValue(ContentEvent.Loading(liveData.value?.data))
        return untilDestroy(
            { data -> liveData.postValue(ContentEvent.Success(data)) },
            { throwable -> liveData.postValue(ContentEvent.Error(throwable, liveData.value?.data)) },
            { liveData.postValue(ContentEvent.Complete(liveData.value?.data)) })
    }

    override fun <T> Observable<out T>.dispatchTo(liveData: MutableLiveData<ContentEvent<T>>): Disposable {
        liveData.postValue(ContentEvent.Loading(liveData.value?.data))
        return untilDestroy(
            { data -> liveData.postValue(ContentEvent.Success(data)) },
            { throwable -> liveData.postValue(ContentEvent.Error(throwable, liveData.value?.data)) },
            { liveData.postValue(ContentEvent.Complete(liveData.value?.data)) })
    }

    override fun <T> Single<out T>.dispatchTo(liveData: MutableLiveData<ContentEvent<T>>): Disposable {
        liveData.postValue(ContentEvent.Loading(liveData.value?.data))
        return untilDestroy(
            { data -> liveData.postValue(ContentEvent.Success(data)) },
            { throwable -> liveData.postValue(ContentEvent.Error(throwable, liveData.value?.data)) })
    }

    override fun <T> Maybe<out T>.dispatchTo(liveData: MutableLiveData<ContentEvent<T>>): Disposable {
        liveData.postValue(ContentEvent.Loading(liveData.value?.data))
        return untilDestroy(
            { data -> liveData.postValue(ContentEvent.Success(data)) },
            { throwable -> liveData.postValue(ContentEvent.Error(throwable, liveData.value?.data)) },
            { liveData.postValue(ContentEvent.Complete(liveData.value?.data)) })
    }

    override fun Completable.dispatchTo(liveData: MutableLiveData<Event>): Disposable {
        liveData.postValue(Event.Loading)
        return untilDestroy(
            { liveData.postValue(Event.Complete) },
            { throwable -> liveData.postValue(Event.Error(throwable)) })
    }

}
