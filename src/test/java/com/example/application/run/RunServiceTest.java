package com.example.application.run;

import com.example.application.account.model.AppUser;
import com.example.application.run.dto.CreateRunRequest;
import com.example.application.weather.Weather;
import com.example.application.weather.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        CreateRunRequest request = new CreateRunRequest(
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
                new BigDecimal("78.5000")
        );
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
}
