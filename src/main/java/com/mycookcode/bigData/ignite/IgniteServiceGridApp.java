package com.mycookcode.bigData.ignite;

import com.mycookcode.bigData.ignite.service.WeatherService;
import com.mycookcode.bigData.ignite.service.WeatherServiceImpl;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

/**
 * Created by zhaolu on 2017/12/13.
 */
public class IgniteServiceGridApp {

    public static void main( String[] args ) throws Exception {
        try (Ignite ignite = Ignition.start()){
            ignite.services().deployClusterSingleton("WeatherService",new WeatherServiceImpl());
            WeatherService service = ignite.services().service("WeatherService");
            String forecast = service.getCurrentTemperature("London", "UK");
            System.out.println("Weather forecast in London:" + forecast);
        }
    }
}
