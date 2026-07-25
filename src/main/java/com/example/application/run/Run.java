package com.example.application.run;

import com.example.application.account.model.AppUser;
import com.example.application.weather.Weather;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Run {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;

    @NotNull
    private LocalDate date;
    @NotNull
    private LocalTime time;
    @NotBlank
    private String car;
    @NotBlank
    private String driver;
    @NotBlank
    private String track;
    @NotBlank
    private String lane;
    @NotNull
    @Digits(integer = 2, fraction = 4)
    private BigDecimal dial;
    @NotNull
    @Digits(integer = 2, fraction = 4)
    private BigDecimal reaction;
    @NotNull
    @Digits(integer = 2, fraction = 4)
    private BigDecimal sixtyFoot;
    @NotNull
    @Digits(integer = 2, fraction = 4)
    private BigDecimal halfTrack;
    @NotNull
    @Digits(integer = 2, fraction = 4)
    private BigDecimal fullTrack;
    @NotNull
    @Digits(integer = 3, fraction = 4)
    private BigDecimal speed;

    // Weather is the owning side of this relationship (holds the run_id FK), so
    // deleting a Run cascades to remove its Weather row.
    @OneToOne(mappedBy = "run", cascade = CascadeType.REMOVE)
    private Weather weather;

    public Run() {
    }

    public Run(AppUser appUser,
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
               BigDecimal speed) {
        this.appUser = appUser;
        this.date = date;
        this.time = time;
        this.car = car;
        this.driver = driver;
        this.track = track;
        this.lane = lane;
        this.dial = dial;
        this.reaction = reaction;
        this.sixtyFoot = sixtyFoot;
        this.halfTrack = halfTrack;
        this.fullTrack = fullTrack;
        this.speed = speed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AppUser getUser() {
        return appUser;
    }

    public void setUser(AppUser appUser) {
        this.appUser = appUser;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public BigDecimal getDial() {
        return dial;
    }

    public void setDial(BigDecimal dial) {
        this.dial = dial;
    }

    public BigDecimal getReaction() {
        return reaction;
    }

    public void setReaction(BigDecimal reaction) {
        this.reaction = reaction;
    }

    public BigDecimal getSixtyFoot() {
        return sixtyFoot;
    }

    public void setSixtyFoot(BigDecimal sixtyFoot) {
        this.sixtyFoot = sixtyFoot;
    }

    public BigDecimal getHalfTrack() {
        return halfTrack;
    }

    public void setHalfTrack(BigDecimal halfTrack) {
        this.halfTrack = halfTrack;
    }

    public BigDecimal getFullTrack() {
        return fullTrack;
    }

    public void setFullTrack(BigDecimal fullTrack) {
        this.fullTrack = fullTrack;
    }

    public BigDecimal getSpeed() {
        return speed;
    }

    public void setSpeed(BigDecimal speed) {
        this.speed = speed;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }
}
