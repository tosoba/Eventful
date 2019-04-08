package com.example.eventsnearby.di

import com.example.nearby.NearbyActionsProvider
import com.example.nearby.NearbyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val nearbyModule = module {
    single { NearbyActionsProvider(get()) }
    viewModel { NearbyViewModel(get()) }
}