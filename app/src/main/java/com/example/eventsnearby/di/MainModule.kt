package com.example.eventsnearby.di

import com.example.coreandroid.navigation.IFragmentProvider
import com.example.eventsnearby.FragmentProvider
import org.koin.dsl.module

val mainModule = module {
    single<IFragmentProvider> {
        FragmentProvider
    }
}