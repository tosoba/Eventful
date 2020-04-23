package com.example.weather

import com.google.android.gms.maps.model.LatLng

sealed class WeatherIntent
class LoadWeather(val latLng: LatLng) : WeatherIntent()