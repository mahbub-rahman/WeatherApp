package com.mahbuburrahman.weatherapp.services;

import com.mahbuburrahman.weatherapp.model.ForecastMainClass;
import com.mahbuburrahman.weatherapp.model.WeatherResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Mahbuburrahman on 1/3/18.
 */

public interface WeatherServices {

    @GET()
    Call<WeatherResponse>getCurrentWeather(@Url String urlString);

    @GET()
    Call<ForecastMainClass>getForecastWeather(@Url String urlString);
}
