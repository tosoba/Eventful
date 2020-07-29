package com.eventful

sealed class MainIntent {
    object LoadLocation : MainIntent()
    object ReloadLocation : MainIntent()
    object PermissionDenied : MainIntent()
}
