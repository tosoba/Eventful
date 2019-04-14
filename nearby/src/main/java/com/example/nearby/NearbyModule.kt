package com.example.nearby

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val nearbyModule = module {
    factory { NearbyActionsProvider(get()) }
    viewModel { NearbyViewModel(get()) }
}