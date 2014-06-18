package com.example;

/**
 * WeatherInfo
 * User: michael.felix
 * Date: 6/16/14
 */
public class WeatherInfo {
    public final String cond;
    public final boolean night;
    public final int temp;
    public String hum;

    public WeatherInfo(String cond, int temp, int hum, boolean night) {
        this.cond = cond;
        this.night = night;
        this.temp = temp;
        this.hum = hum + "%";
    }

    public String displayTemp(){
        return temp + (temp >= 100 ? "°" : "°F");
    }

    public int getIcon() {
        String cond = this.cond.replaceFirst("nt_", "");
        if ("chanceflurries".equals(cond))
            return night ? R.drawable.nt_chanceflurries : R.drawable.chanceflurries;
        else if ("chancerain".equals(cond))
            return night ? R.drawable.nt_chancerain : R.drawable.chancerain;
        else if ("chancesleet".equals(cond))
            return night ? R.drawable.nt_chancesleet : R.drawable.chancesleet;
        else if ("chancesnow".equals(cond))
            return night ? R.drawable.nt_chancesnow : R.drawable.chancesnow;
        else if ("chancetstorms".equals(cond))
            return night ? R.drawable.nt_chancetstorms : R.drawable.chancetstorms;
        else if ("cloudy".equals(cond))
            return night ? R.drawable.nt_cloudy : R.drawable.cloudy;
        else if ("flurries".equals(cond))
            return night ? R.drawable.nt_flurries : R.drawable.flurries;
        else if ("fog".equals(cond))
            return night ? R.drawable.nt_fog : R.drawable.fog;
        else if ("hazy".equals(cond))
            return night ? R.drawable.nt_hazy : R.drawable.hazy;
        else if ("rain".equals(cond))
            return night ? R.drawable.nt_rain : R.drawable.rain;
        else if ("snow".equals(cond))
            return night ? R.drawable.nt_snow : R.drawable.snow;
        else if ("sunny".equals(cond))
            return night ? R.drawable.nt_sunny : R.drawable.sunny;
        else if ("clear".equals(cond))
            return night ? R.drawable.nt_clear : R.drawable.clear;
        else if ("sleet".equals(cond))
            return night ? R.drawable.nt_sleet : R.drawable.sleet;
        else if ("tstorms".equals(cond))
            return night ? R.drawable.nt_tstorms : R.drawable.tstorms;
        else if ("mostlycloudy".equals(cond))
            return night ? R.drawable.nt_mostlycloudy : R.drawable.mostlycloudy;
        else if ("partlycloudy".equals(cond))
            return night ? R.drawable.nt_partlycloudy : R.drawable.partlycloudy;
        else if ("mostlysunny".equals(cond))
            return night ? R.drawable.nt_mostlysunny : R.drawable.mostlysunny;
        else if ("partlysunny".equals(cond))
            return night ? R.drawable.nt_partlysunny : R.drawable.partlysunny;
        else
            return R.drawable.blank;
    }

}
