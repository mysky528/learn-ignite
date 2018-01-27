package com.mycookcode.bigData.ignite.service;

import org.apache.ignite.services.ServiceContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhaolu on 2017/12/13.
 */
public class WeatherServiceImpl implements WeatherService {

    final String WEATHER_URL = "http://samples.openweathermap.org/data/2.5/weather?";

    private static final String appId = "ca7345b4a1ef8c037f7749c09fcbf808";

    @Override
    public void init(ServiceContext ctx)
    {
        System.out.println("Weather Service is initialized!");
    }

    @Override
    public void execute(ServiceContext ctx) throws Exception {
        System.out.println("Weather Service is started!");
    }

    @Override
    public void cancel(ServiceContext ctx) {
        System.out.println("Weather Service is stopped!");
    }

    @Override
    public String getCurrentTemperature(String cityName,
                                        String countryCode) throws Exception
    {
        System.out.println(">>> Requested weather forecast [city="
                + cityName + ", countryCode=" + countryCode + "]");

        String connStr = WEATHER_URL + "q=" + cityName + ","
                + countryCode + "&appid=" + appId;

        URL url = new URL(connStr);

        HttpURLConnection conn = null;
        try {
            // Connecting to the weather service.
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            conn.connect();

            // Read data from the weather server.
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {

                String line;
                StringBuilder builder = new StringBuilder();

                while ((line = reader.readLine()) != null)
                    builder.append(line);
                System.out.println("============" + builder.toString());
                return builder.toString();
            }
        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }

}
