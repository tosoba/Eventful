package com.eventful

import android.util.Log
import com.eventful.core.android.notification.AlarmNotifications
import com.eventful.core.android.view.BannerSliderImageLoadingService
import com.eventful.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import ss.com.bannerslider.Slider

@ExperimentalCoroutinesApi
@FlowPreview
class EventfulApp : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = DaggerAppComponent
        .factory()
        .create(this)

    override fun onCreate() {
        super.onCreate()
        Slider.init(BannerSliderImageLoadingService)
        RxJavaPlugins.setErrorHandler {
            Log.e("Rx error", it?.message ?: "Unknown message")
        }
        AlarmNotifications.createChannel(this)
    }
}
