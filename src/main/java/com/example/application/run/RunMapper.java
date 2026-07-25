package com.example.application.run;

import com.example.application.account.model.AppUser;
import com.example.application.run.dto.CreateRunRequest;
import com.example.application.run.dto.RunResponse;
import com.example.application.run.dto.UpdateRunRequest;
import com.example.application.weather.WeatherMapper;

public class RunMapper {

    private RunMapper() {
    }

    public static Run toEntity(CreateRunRequest request, AppUser owner) {
        return new Run(
                owner,
                request.date(),
                request.time(),
                request.car(),
                request.driver(),
                request.track(),
                request.lane(),
                request.dial(),
                request.reaction(),
                request.sixtyFoot(),
                request.halfTrack(),
                request.fullTrack(),
                request.speed()
        );
    }

    public static void applyUpdate(Run run, UpdateRunRequest request) {
        run.setDate(request.date());
        run.setTime(request.time());
        run.setCar(request.car());
        run.setDriver(request.driver());
        run.setTrack(request.track());
        run.setLane(request.lane());
        run.setDial(request.dial());
        run.setReaction(request.reaction());
        run.setSixtyFoot(request.sixtyFoot());
        run.setHalfTrack(request.halfTrack());
        run.setFullTrack(request.fullTrack());
        run.setSpeed(request.speed());
    }

    public static RunResponse toResponse(Run run) {
        return new RunResponse(
                run.getId(),
                run.getDate(),
                run.getTime(),
                run.getCar(),
                run.getDriver(),
                run.getTrack(),
                run.getLane(),
                run.getDial(),
                run.getReaction(),
                run.getSixtyFoot(),
                run.getHalfTrack(),
                run.getFullTrack(),
                run.getSpeed(),
                WeatherMapper.toResponse(run.getWeather())
        );
    }
}
