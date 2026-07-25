package com.example.application.weather.dto;

import java.math.BigDecimal;

public record WeatherResponse(
        BigDecimal temperature,
        BigDecimal relativeHumidity,
        BigDecimal uncorrectedBarometer,
        BigDecimal correctedBarometer,
        BigDecimal windSpeed,
        BigDecimal windDirection,
        BigDecimal dewPoint,
        BigDecimal saturationPressure,
        BigDecimal vaporPressure,
        BigDecimal grains,
        BigDecimal airDensityNoWaterVapor,
        BigDecimal airDensityWithWaterVapor,
        BigDecimal densityAltitude
) {
}
