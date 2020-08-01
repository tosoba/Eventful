package com.eventful.core.android.util.ext

inline fun <reified R> Any?.castTo(): R? = this as? R
