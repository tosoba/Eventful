package com.example.eventsnearby

sealed class MainIntent
object LoadLocation : MainIntent()
object PermissionDenied : MainIntent()