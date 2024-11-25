package org.example.service;

import org.example.entity.vo.response.WeatherVO;

public interface WeatherService {
    WeatherVO fetchWeather(double longitude, double latitude);
}
