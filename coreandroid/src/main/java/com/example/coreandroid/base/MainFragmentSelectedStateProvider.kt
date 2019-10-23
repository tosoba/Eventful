package com.example.coreandroid.base

import kotlinx.coroutines.flow.Flow

interface MainFragmentSelectedStateProvider {
    fun <T> isSelectedFlow(fragmentClass: Class<T>): Flow<Boolean>
}