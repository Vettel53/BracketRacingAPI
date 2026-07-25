package com.example.application.weather;

import com.example.application.run.Run;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;

/**
 * All numeric fields used to be stored as String (including the literal text
 * "null" for anything the upstream API didn't return). That meant you couldn't
 * do math, sort, or compare on weather data at all - which defeats the entire
 * point of correlating weather with race performance. Now they're proper
 * nullable BigDecimals.
 */
@Entity
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "run_id")
    private Run run;

    private BigDecimal temperature;
    private BigDecimal relativeHumidity;
    private BigDecimal uncorrectedBarometer;
    private BigDecimal correctedBarometer;
    private BigDecimal windSpeed;
    private BigDecimal windDirection;
    private BigDecimal dewPoint;
    private BigDecimal saturationPressure;
    private BigDecimal vaporPressure;
    private BigDecimal grains;
    private BigDecimal airDensityNoWaterVapor;
    private BigDecimal airDensityWithWaterVapor;
    private BigDecimal densityAltitude;

    public Weather() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Run getRun() {
        return run;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public BigDecimal getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(BigDecimal relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    public BigDecimal getUncorrectedBarometer() {
        return uncorrectedBarometer;
    }

    public void setUncorrectedBarometer(BigDecimal uncorrectedBarometer) {
        this.uncorrectedBarometer = uncorrectedBarometer;
    }

    public BigDecimal getCorrectedBarometer() {
        return correctedBarometer;
    }

    public void setCorrectedBarometer(BigDecimal correctedBarometer) {
        this.correctedBarometer = correctedBarometer;
    }

    public BigDecimal getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(BigDecimal windSpeed) {
        this.windSpeed = windSpeed;
    }

    public BigDecimal getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(BigDecimal windDirection) {
        this.windDirection = windDirection;
    }

    public BigDecimal getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(BigDecimal dewPoint) {
        this.dewPoint = dewPoint;
    }

    public BigDecimal getSaturationPressure() {
        return saturationPressure;
    }

    public void setSaturationPressure(BigDecimal saturationPressure) {
        this.saturationPressure = saturationPressure;
    }

    public BigDecimal getVaporPressure() {
        return vaporPressure;
    }

    public void setVaporPressure(BigDecimal vaporPressure) {
        this.vaporPressure = vaporPressure;
    }

    public BigDecimal getGrains() {
        return grains;
    }

    public void setGrains(BigDecimal grains) {
        this.grains = grains;
    }

    public BigDecimal getAirDensityNoWaterVapor() {
        return airDensityNoWaterVapor;
    }

    public void setAirDensityNoWaterVapor(BigDecimal airDensityNoWaterVapor) {
        this.airDensityNoWaterVapor = airDensityNoWaterVapor;
    }

    public BigDecimal getAirDensityWithWaterVapor() {
        return airDensityWithWaterVapor;
    }

    public void setAirDensityWithWaterVapor(BigDecimal airDensityWithWaterVapor) {
        this.airDensityWithWaterVapor = airDensityWithWaterVapor;
    }

    public BigDecimal getDensityAltitude() {
        return densityAltitude;
    }

    public void setDensityAltitude(BigDecimal densityAltitude) {
        this.densityAltitude = densityAltitude;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "id=" + id +
                ", temperature=" + temperature +
                ", relativeHumidity=" + relativeHumidity +
                ", windSpeed=" + windSpeed +
                ", dewPoint=" + dewPoint +
                '}';
    }
}
