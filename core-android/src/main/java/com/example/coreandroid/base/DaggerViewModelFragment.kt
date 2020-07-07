package com.example.coreandroid.base

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.coreandroid.di.viewmodel.InjectingSavedStateViewModelFactory
import dagger.android.support.DaggerFragment
import javax.inject.Inject
import javax.inject.Provider

open class DaggerViewModelFragment<VM : ViewModel> : DaggerFragment {
    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    @Inject
    internal lateinit var viewModelProvider: Provider<VM>
    protected val viewModel: VM get() = viewModelProvider.get()
}

inline fun <reified VM : ViewModel> DaggerViewModelFragment<VM>.savedStateViewModelFrom(
    factory: InjectingSavedStateViewModelFactory,
    defaultArgs: Bundle? = (this as? HasArgs)?.args
): VM {
    val savedStateFactory = factory.create(this, defaultArgs)
    return ViewModelProvider(this, savedStateFactory)[VM::class.java]
}

interface HasArgs {
    val args: Bundle
}
