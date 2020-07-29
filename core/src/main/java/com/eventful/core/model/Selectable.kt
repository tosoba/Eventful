package com.eventful.core.model

data class Selectable<T>(val item: T, val selected: Boolean = false)
