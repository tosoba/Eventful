package com.example.weather

import com.airbnb.epoxy.EpoxyModelGroup

class WeatherEpoxyModelGroup(
    temperatureInLocation: TemperatureInLocationBindingModel_,
    description: WeatherDescriptionBindingModel_
) : EpoxyModelGroup(R.layout.weather_currently, temperatureInLocation, description)
