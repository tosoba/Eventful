package com.example.eventsnearby

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.coreandroid.util.ext.registerFragmentLifecycleCallbacks
import com.example.coreandroid.view.BannerSliderImageLoadingService
import com.example.eventsnearby.di.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import io.reactivex.plugins.RxJavaPlugins
import ss.com.bannerslider.Slider


class EventfulApp : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> = DaggerAppComponent
        .builder()
        .application(this)
        .create(this)

    override fun onCreate() {
        super.onCreate()
        Slider.init(BannerSliderImageLoadingService)
//        registerLifecycleCallbacks()
        RxJavaPlugins.setErrorHandler {
            Log.e("Rx error", it?.message ?: "Unknown text")
        }
    }

    private fun registerLifecycleCallbacks() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(
                activity: Activity, savedInstanceState: Bundle?
            ) = handleActivityCreated(activity)

            override fun onActivityStarted(activity: Activity) = Unit

            override fun onActivityResumed(activity: Activity) = Unit

            override fun onActivityPaused(activity: Activity) = Unit

            override fun onActivityStopped(activity: Activity) = Unit

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) = Unit

            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }

    private fun handleActivityCreated(activity: Activity) {
        activity.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                if (f is DaggerFragment) AndroidSupportInjection.inject(f)
            }
        }, true)
    }
}