package com.example.coreandroid.base

import android.content.Context
import android.os.Handler
import androidx.fragment.app.Fragment
import com.example.coreandroid.di.Dependencies
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject
import javax.inject.Named

open class InjectableFragment : Fragment(), HasSupportFragmentInjector {

    val fragmentScope by lazy { CoroutineScope(Dispatchers.Main + Job()) }

    @Inject
    internal lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>

    override fun supportFragmentInjector(): AndroidInjector<Fragment> = childFragmentInjector

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentScope.cancel()
    }
}

open class InjectableEpoxyFragment : InjectableFragment() {

    @Inject
    @field:Named(Dependencies.EPOXY_DIFFER)
    internal lateinit var differ: Handler

    @Inject
    @field:Named(Dependencies.EPOXY_BUILDER)
    internal lateinit var builder: Handler
}