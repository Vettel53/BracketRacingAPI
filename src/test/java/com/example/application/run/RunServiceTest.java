package com.example.application.run;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.application.account.model.AppUser;
import com.example.application.run.dto.CreateRunRequest;
import com.example.application.run.dto.UpdateRunRequest;
import com.example.application.weather.Weather;
import com.example.application.weather.WeatherService;

@ExtendWith(MockitoExtension.class)
class RunServiceTest {

    @Mock
    private RunRepo runRepo;

    @Mock
    private WeatherService weatherService;

    private RunService runService;

    @BeforeEach
    void setUp() {
        runService = new RunService(runRepo, weatherService);
        // fakeWeatherGeneration is normally injected from the weatherapi.fake-weather
        // property via @Value - there's no Spring context in a plain unit test, so
        // it has to be set directly.
        ReflectionTestUtils.setField(runService, "fakeWeatherGeneration", true);
    }

    @Test
    void createRun_withFakeWeatherEnabled_attachesFakeWeatherAndSavesRun() {
        // Arrange
        AppUser owner = new AppUser("jdoe", "hashed-password");
        CreateRunRequest request = createSampleRunRequest();
        Weather fakeWeather = new Weather();
        when(weatherService.getFakeWeather()).thenReturn(fakeWeather);

        // Act
        Run createdRun = runService.createRun(owner, request);

        // Assert
        assertThat(createdRun.getTrack()).isEqualTo("Atlanta Dragway");
        assertThat(createdRun.getUser()).isEqualTo(owner);
        assertThat(createdRun.getWeather()).isEqualTo(fakeWeather);
        verify(weatherService, never()).getCurrentWeather(any(), any(), any());
        verify(weatherService).saveWeather(fakeWeather);
        verify(runRepo).save(createdRun);
    }

    @Test
    void createRun_withFakeWeatherDisabled_attachesCurrentWeatherAndSavesRun() {
        // Arrange
        ReflectionTestUtils.setField(runService, "fakeWeatherGeneration", false);
        AppUser owner = new AppUser("jdoe", "hashed-password");
        CreateRunRequest request = createSampleRunRequest();
        Weather currentWeather = new Weather();
        when(weatherService.getCurrentWeather(any(), any(), any())).thenReturn(currentWeather);

        // Act
        Run createdRun = runService.createRun(owner, request);

        // Assert
        assertThat(createdRun.getTrack()).isEqualTo("Atlanta Dragway");
        assertThat(createdRun.getUser()).isEqualTo(owner);
        assertThat(createdRun.getWeather()).isEqualTo(currentWeather);
        verify(weatherService).getCurrentWeather(any(), any(), any());
        verify(weatherService).saveWeather(currentWeather);
        verify(runRepo).save(createdRun);
    }

    @Test
    void updateRun_withValidRun_returnsNewRun() {
        // Arrange
        Run existingRun = createSampleRun();
        UpdateRunRequest updateRequest = new UpdateRunRequest(
                LocalDate.of(2026, 7, 26),
                LocalTime.of(15, 0),
                "1970 Mustang",
                "jdoe",
                "Atlanta Dragway",
                "Right",
                new BigDecimal("8.8000"),
                new BigDecimal("0.0100"),
                new BigDecimal("1.6000"),
                new BigDecimal("5.6000"),
                new BigDecimal("8.7500"),
                new BigDecimal("79.0000"));

        // Act
        when(runRepo.save(any(Run.class))).then(returnsFirstArg());
        Run updatedRun = runService.updateRun(existingRun, updateRequest);

        // Assert
        assertThat(updatedRun.getDate()).isEqualTo(LocalDate.of(2026, 7, 26));
        assertThat(updatedRun.getTime()).isEqualTo(LocalTime.of(15, 0));
        assertThat(updatedRun.getCar()).isEqualTo("1970 Mustang");
        assertThat(updatedRun.getLane()).isEqualTo("Right");
        assertThat(updatedRun.getDial()).isEqualByComparingTo(new BigDecimal("8.8000"));
        assertThat(updatedRun.getReaction()).isEqualByComparingTo(new BigDecimal("0.0100"));
        assertThat(updatedRun.getSixtyFoot()).isEqualByComparingTo(new BigDecimal("1.6000"));
        assertThat(updatedRun.getHalfTrack()).isEqualByComparingTo(new BigDecimal("5.6000"));
        assertThat(updatedRun.getFullTrack()).isEqualByComparingTo(new BigDecimal("8.7500"));
        assertThat(updatedRun.getSpeed()).isEqualByComparingTo(new BigDecimal("79.0000"));
    }

    @Test
    void updateRun_withNullFields_doesNotChangeThoseFields() {
        // Arrange
        Run existingRun = createSampleRun();
        UpdateRunRequest updateRequest = new UpdateRunRequest(
                null, // date
                null, // time
                null, // car
                null, // username
                null, // track
                null, // lane
                null, // dial
                null, // reaction
                null, // sixtyFoot
                null, // halfTrack
                null, // fullTrack
                null // speed
        );

        // Act
        when(runRepo.save(any(Run.class))).then(returnsFirstArg());
        Run updatedRun = runService.updateRun(existingRun, updateRequest);

        // Assert - all fields should remain unchanged
        assertThat(updatedRun.getDate()).isEqualTo(existingRun.getDate());
        assertThat(updatedRun.getTime()).isEqualTo(existingRun.getTime());
        assertThat(updatedRun.getCar()).isEqualTo(existingRun.getCar());
        assertThat(updatedRun.getLane()).isEqualTo(existingRun.getLane());
        assertThat(updatedRun.getDial()).isEqualByComparingTo(existingRun.getDial());
        assertThat(updatedRun.getReaction()).isEqualByComparingTo(existingRun.getReaction());
        assertThat(updatedRun.getSixtyFoot()).isEqualByComparingTo(existingRun.getSixtyFoot());
        assertThat(updatedRun.getHalfTrack()).isEqualByComparingTo(existingRun.getHalfTrack());
        assertThat(updatedRun.getFullTrack()).isEqualByComparingTo(existingRun.getFullTrack());
        assertThat(updatedRun.getSpeed()).isEqualByComparingTo(existingRun.getSpeed());
    }

    Run createSampleRun() {
        return new Run(
                new AppUser("jdoe", "hashed-password"),
                LocalDate.of(2026, 7, 25),
                LocalTime.of(14, 30),
                "1969 Camaro",
                "jdoe",
                "Atlanta Dragway",
                "Left",
                new BigDecimal("8.9000"),
                new BigDecimal("0.0120"),
                new BigDecimal("1.6500"),
                new BigDecimal("5.7000"),
                new BigDecimal("8.8500"),
                new BigDecimal("78.5000"));
    }

    CreateRunRequest createSampleRunRequest() {
        return new CreateRunRequest(
                LocalDate.of(2026, 7, 25),
                LocalTime.of(14, 30),
                "1969 Camaro",
                "jdoe",
                "Atlanta Dragway",
                "Left",
                new BigDecimal("8.9000"),
                new BigDecimal("0.0120"),
                new BigDecimal("1.6500"),
                new BigDecimal("5.7000"),
                new BigDecimal("8.8500"),
                new BigDecimal("78.5000"));
    }
}
