package com.example;

/**
 * Conditions
 * User: michael.felix
 * Date: 6/14/14
 */
public class Conditions {
    CurrentObservation current_observation;

    public static class CurrentObservation {
        String icon;
        String weather;
        String temp_f;
        String feelslike_f;
        String relative_humidity;
        String wind_string;
        String wind_dir;
        String precip_today_in;
    }
}
