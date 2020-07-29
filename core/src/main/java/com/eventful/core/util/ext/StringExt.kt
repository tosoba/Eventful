package com.eventful.core.util.ext

import java.util.*

val String.lowerCasedTrimmed: String get() = toLowerCase(Locale.getDefault()).trim()
