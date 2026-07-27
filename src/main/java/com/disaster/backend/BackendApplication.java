package com.disaster.backend;

import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    // Weather API Endpoint
    @GetMapping("/api/v1/weather/telemetry")
    public SimpleWeatherResponse getWeatherData(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "Sector") String city,
            @RequestParam(defaultValue = "Region") String country) {

        RestTemplate restTemplate = new RestTemplate();
        String url = String.format(
            "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,relative_humidity_2m,rain,surface_pressure,wind_speed_10m,weather_code&hourly=temperature_2m&timezone=auto",
            lat, lon
        );

        SimpleWeatherResponse data = new SimpleWeatherResponse();
        try {
            // Raw response as Map (No Jackson needed!)
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("current")) {
                Map<?, ?> current = (Map<?, ?>) response.get("current");

                data.locationName = city;
                data.country = country;
                data.temperature = ((Number) current.get("temperature_2m")).doubleValue();
                data.humidity = ((Number) current.get("relative_humidity_2m")).intValue();
                data.pressure = (int) Math.round(((Number) current.get("surface_pressure")).doubleValue());
                data.windSpeed = (int) Math.round(((Number) current.get("wind_speed_10m")).doubleValue());
                data.rainfall = current.containsKey("rain") ? ((Number) current.get("rain")).doubleValue() : 0.0;

                if (data.rainfall >= 3.5) {
                    data.alertLevel = "RED";
                    data.alertMessage = "HEAVY RAIN WARNING!";
                } else if (data.rainfall > 0) {
                    data.alertLevel = "ORANGE";
                    data.alertMessage = "MODERATE RAIN WATCH";
                } else {
                    data.alertLevel = "GREEN";
                    data.alertMessage = "CONDITIONS SAFE";
                }
            }
        } catch (Exception e) {
            data.locationName = city;
            data.country = country;
            data.alertLevel = "GREEN";
            data.alertMessage = "ERROR FETCHING WEATHER DATA";
        }
        return data;
    }
}

class SimpleWeatherResponse {
    public String locationName;
    public String country;
    public double temperature;
    public int humidity;
    public int pressure;
    public int windSpeed;
    public double rainfall;
    public String alertLevel;
    public String alertMessage;
}