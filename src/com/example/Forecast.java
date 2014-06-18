package com.example;

/**
 * Conditions
 * User: michael.felix
 * Date: 6/14/14
 */
public class Forecast {
    FCast forecast;

    public static class Date {
        int month, day, hour;
    }

    public static class Wind {
        String mph, dir;
    }

    public static class Precip {
        float in;
    }

    public static class Temp {
        String fahrenheit;
    }

    public static class SimpleForecastDay {
        Date date;
        Temp high, low;
        String conditions, icon;
        Precip qpf_allday, snow_allday;
        Wind avewind, maxwind;
        int avehumidity, maxhumidity;
    }

    public static class TextForecastDay {
        int period;
        String icon;
        String title;
        String fcttext;
    }

    public static class SimpleForecast {
        SimpleForecastDay[] forecastday;
    }

    public static class TextForecast {
        String date;
        TextForecastDay[] forecastday;
    }

    public static class FCast {
        TextForecast txt_forecast;
        SimpleForecast simpleforecast;
    }
}
