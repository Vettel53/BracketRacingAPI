package com.example.application.weather;

import com.example.application.weather.dto.WeatherResponse;

public class WeatherMapper {

    private WeatherMapper() {
    }

    public static WeatherResponse toResponse(Weather weather) {
        if (weather == null) {
            return null;
        }
        return new WeatherResponse(
                weather.getTemperature(),
                weather.getRelativeHumidity(),
                weather.getUncorrectedBarometer(),
                weather.getCorrectedBarometer(),
                weather.getWindSpeed(),
                weather.getWindDirection(),
                weather.getDewPoint(),
                weather.getSaturationPressure(),
                weather.getVaporPressure(),
                weather.getGrains(),
                weather.getAirDensityNoWaterVapor(),
                weather.getAirDensityWithWaterVapor(),
                weather.getDensityAltitude()
        );
    }
}
