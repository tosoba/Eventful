package com.example.nearby

import androidx.lifecycle.LifecycleOwner
import com.example.coreandroid.model.EventUiModel

sealed class NearbyViewEvent

sealed class Interaction : NearbyViewEvent() {
    object EventListScrolledToEnd : Interaction()
    data class EventClicked(val event: EventUiModel) : Interaction()
}

sealed class Lifecycle : NearbyViewEvent() {
    data class OnViewCreated(val lifecycleOwner: LifecycleOwner) : Lifecycle()
    object OnDestroy : Lifecycle()
}

