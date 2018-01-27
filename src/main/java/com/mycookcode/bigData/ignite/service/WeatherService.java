package com.mycookcode.bigData.ignite.service;

import org.apache.ignite.services.Service;

/**
 * Created by zhaolu on 2017/12/13.
 */
public interface WeatherService extends Service{
    /**
     * Get a current temperature for a specific city in the world.
     *
     * @param countryCode Country code (ISO 3166 country codes).
     * @param cityName City name.
     * @return Current temperature in the city in JSON format.
     * @throws Exception if an exception happened.
     */
    String getCurrentTemperature(String countryCode,String cityName)throws Exception;

}
