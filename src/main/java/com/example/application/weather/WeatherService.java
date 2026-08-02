package com.example.application.weather;

import com.example.application.run.Run;
import com.example.application.shared.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class WeatherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherService.class);

    private final WebClient webClient;
    private final WeatherRepo weatherRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${weatherapi.local}")
    private boolean localWeatherAPI;

    public WeatherService(WeatherRepo weatherRepo) {
        this.webClient = WebClient.builder().build();
        this.weatherRepo = weatherRepo;
    }

    // Used for fake run generation and local dev/testing without hitting the real API
    public Weather getFakeWeather() {
        Weather fakeWeather = new Weather();
        fakeWeather.setTemperature(new BigDecimal("75.0"));
        fakeWeather.setRelativeHumidity(new BigDecimal("45.0"));
        fakeWeather.setUncorrectedBarometer(new BigDecimal("29.92"));
        fakeWeather.setCorrectedBarometer(new BigDecimal("29.92"));
        fakeWeather.setWindSpeed(new BigDecimal("5.0"));
        fakeWeather.setWindDirection(new BigDecimal("180"));
        fakeWeather.setDewPoint(new BigDecimal("50.0"));
        fakeWeather.setSaturationPressure(new BigDecimal("0.43"));
        fakeWeather.setVaporPressure(new BigDecimal("0.19"));
        fakeWeather.setGrains(new BigDecimal("55"));
        fakeWeather.setAirDensityNoWaterVapor(new BigDecimal("0.0740"));
        fakeWeather.setAirDensityWithWaterVapor(new BigDecimal("0.0738"));
        fakeWeather.setDensityAltitude(new BigDecimal("1200"));
        return fakeWeather;
    }

    public Weather getCurrentWeather(String raceTrack, LocalDate date, LocalTime time) {
        URI weatherURI = UriComponentsBuilder
                .fromUri(getAPIURL())
                .queryParam("trackName", raceTrack)
                .queryParam("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .queryParam("time", time.format(DateTimeFormatter.ofPattern("HH:mm")))
                .build()
                .toUri();

        String jsonResponse;
        try {
            jsonResponse = webClient.get()
                    .uri(weatherURI)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            LOGGER.error("Weather API request failed for track {}", raceTrack, e);
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "WEATHER_SERVICE_UNAVAILABLE",
                    "Weather service is currently unavailable, please try again later");
        }

        return parseWeatherAPIResponse(jsonResponse);
    }

    /**
     * Refreshes weather for a run in place (keeps the same Weather row/id) rather
     * than inserting a new row, since Weather is a strict 1:1 with Run.
     */
    public void updateWeather(Run runToEdit, String newTrack, LocalDate editDate, LocalTime editTime) {
        Weather currentWeather = runToEdit.getWeather();
        Weather freshWeather = getCurrentWeather(newTrack, editDate, editTime);

        currentWeather.setTemperature(freshWeather.getTemperature());
        currentWeather.setRelativeHumidity(freshWeather.getRelativeHumidity());
        currentWeather.setWindSpeed(freshWeather.getWindSpeed());
        currentWeather.setDewPoint(freshWeather.getDewPoint());
        // Remaining fields (barometer, grains, air density, etc.) aren't provided by
        // the current TrackWeatherAPI response and stay at their last known value.

        weatherRepo.save(currentWeather);
    }

    private URI getAPIURL() {
        if (localWeatherAPI) {
            return URI.create("http://localhost:8081/weather/by-datetime");
        }
        return URI.create("https://trackweatherapi-production.up.railway.app/weather");
    }

    private Weather parseWeatherAPIResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode dataRow = root.path("data").get(0);

            Weather parsed = new Weather();
            parsed.setTemperature(decimalOrNull(dataRow, "temp"));
            parsed.setRelativeHumidity(decimalOrNull(dataRow, "humidity"));
            parsed.setWindSpeed(decimalOrNull(dataRow, "wind_speed"));
            parsed.setDewPoint(decimalOrNull(dataRow, "dew_point"));
            // Barometer/grains/air-density/density-altitude aren't exposed by the
            // upstream API yet - they stay null until TrackWeatherAPI adds them.
            return parsed;
        } catch (Exception e) {
            LOGGER.error("Failed to parse weather API response: {}", jsonResponse, e);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "WEATHER_PARSE_ERROR", "Unable to parse weather data");
        }
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : new BigDecimal(value.asText());
    }

    public void saveWeather(Weather trackWeather) {
        weatherRepo.save(trackWeather);
    }
}
