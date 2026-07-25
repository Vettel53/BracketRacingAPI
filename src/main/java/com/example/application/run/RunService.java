package com.example.application.run;

import com.example.application.account.model.AppUser;
import com.example.application.run.dto.CreateRunRequest;
import com.example.application.run.dto.UpdateRunRequest;
import com.example.application.weather.Weather;
import com.example.application.weather.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Service
public class RunService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunService.class);
    private static final Random RANDOM = new Random();

    private final WeatherService weatherService;
    private final RunRepo runRepo;

    @Value("${weatherapi.fake-weather}")
    private boolean fakeWeatherGeneration;

    public RunService(RunRepo runRepo, WeatherService weatherService) {
        this.runRepo = runRepo;
        this.weatherService = weatherService;
    }

    public List<Run> getUserRuns(AppUser appUser) {
        return runRepo.findByAppUser(appUser);
    }

    @Transactional
    public Run createRun(AppUser owner, CreateRunRequest request) {
        Run run = RunMapper.toEntity(request, owner);
        runRepo.save(run);

        Weather trackWeather = fakeWeatherGeneration
                ? weatherService.getFakeWeather()
                : weatherService.getCurrentWeather(run.getTrack(), run.getDate(), run.getTime());

        trackWeather.setRun(run);
        run.setWeather(trackWeather);
        weatherService.saveWeather(trackWeather);

        LOGGER.info("Created run {} for user {}", run.getId(), owner.getUsername());
        return run;
    }

    /**
     * Generates a run with randomized-but-plausible times for demoing/testing.
     * Used to live in a standalone, non-Spring-managed FakeRunGenerationService -
     * folded in here so it's an actual testable service method instead of a
     * static utility nobody could mock.
     */
    @Transactional
    public Run createFakeRun(AppUser owner) {
        String lane = RANDOM.nextBoolean() ? "Left" : "Right";
        BigDecimal dial = randomDecimal(4.44, 4.57, 4);
        BigDecimal reaction = randomDecimal(0.0, 0.30, 4);
        BigDecimal sixtyFoot = randomDecimal(0.995, 0.999, 4);
        BigDecimal halfTrack = randomDecimal(2.870, 2.874, 4);
        BigDecimal fullTrack = dial.add(randomDecimal(-0.016, 0.016, 4));
        BigDecimal speed = randomDecimal(153.0, 156.0, 2);

        CreateRunRequest fakeRequest = new CreateRunRequest(
                LocalDate.now(), LocalTime.now(), "Burple Dragster", "Max",
                "Texas Motorplex", lane, dial, reaction, sixtyFoot, halfTrack, fullTrack, speed
        );

        return createRun(owner, fakeRequest);
    }

    @Transactional
    public Run updateRun(Run runToEdit, UpdateRunRequest request) {
        RunMapper.applyUpdate(runToEdit, request);
        Run saved = runRepo.save(runToEdit);
        LOGGER.info("Updated run {}", saved.getId());
        return saved;
    }

    @Transactional
    public void deleteRun(Run runToDelete) {
        LOGGER.info("Deleting run {}", runToDelete.getId());
        runRepo.delete(runToDelete);
    }

    private BigDecimal randomDecimal(double min, double max, int scale) {
        double value = min + RANDOM.nextDouble() * (max - min);
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }
}
