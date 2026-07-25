package com.example.application.run.dto;

import com.example.application.weather.dto.WeatherResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record RunResponse(
        Long id,
        LocalDate date,
        LocalTime time,
        String car,
        String driver,
        String track,
        String lane,
        BigDecimal dial,
        BigDecimal reaction,
        BigDecimal sixtyFoot,
        BigDecimal halfTrack,
        BigDecimal fullTrack,
        BigDecimal speed,
        WeatherResponse weather
) {
}
