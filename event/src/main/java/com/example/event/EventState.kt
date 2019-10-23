package com.example.event

import com.example.coreandroid.util.Data
import com.example.coreandroid.util.Initial
import com.haroldadmin.vector.VectorState

data class EventState(
    val isFavourite: Data<Boolean>
) : VectorState {
    companion object {
        val INITIAL: EventState
            get() = EventState(isFavourite = Data(false, Initial))
    }
}