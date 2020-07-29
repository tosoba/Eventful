package com.eventful.core.android.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eventful.core.android.di.viewmodel.InjectingSavedStateViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject
import javax.inject.Provider

open class DaggerViewModelActivity<VM : ViewModel> : DaggerAppCompatActivity {
    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    @Inject
    internal lateinit var viewModelProvider: Provider<VM>
    protected val viewModel: VM get() = viewModelProvider.get()
}

inline fun <reified VM : ViewModel> DaggerViewModelActivity<VM>.savedStateViewModelFrom(
    factory: InjectingSavedStateViewModelFactory
): VM = ViewModelProvider(this, factory.create(this))[VM::class.java]
