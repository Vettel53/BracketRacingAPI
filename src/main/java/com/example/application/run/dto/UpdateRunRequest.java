package com.example.application.run.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateRunRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        @NotBlank String car,
        @NotBlank String driver,
        @NotBlank String track,
        @NotBlank String lane,
        @NotNull @Digits(integer = 2, fraction = 4) BigDecimal dial,
        @NotNull @Digits(integer = 2, fraction = 4) BigDecimal reaction,
        @NotNull @Digits(integer = 2, fraction = 4) BigDecimal sixtyFoot,
        @NotNull @Digits(integer = 2, fraction = 4) BigDecimal halfTrack,
        @NotNull @Digits(integer = 2, fraction = 4) BigDecimal fullTrack,
        @NotNull @Digits(integer = 3, fraction = 4) BigDecimal speed
) {
}
