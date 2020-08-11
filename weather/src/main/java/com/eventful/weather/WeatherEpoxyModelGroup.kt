package com.eventful.weather

import com.airbnb.epoxy.EpoxyModelGroup

class WeatherEpoxyModelGroup(
    temperatureInLocation: TemperatureInLocationBindingModel_,
    forecastInfo: WeatherSymbolInfoBindingModel_,
    windInfo: WeatherSymbolInfoBindingModel_,
    humidityInfo: WeatherSymbolInfoBindingModel_,
    description: WeatherDescriptionBindingModel_
) : EpoxyModelGroup(
    R.layout.weather_group,
    temperatureInLocation,
    forecastInfo,
    windInfo,
    humidityInfo,
    description
)
