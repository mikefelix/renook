package com.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForecastInfo extends WeatherInfo {
    public static final Pattern chanceRainPattern = Pattern.compile("[Cc]hance of (rain|(thunder)?storms|precipitation) *(around|near)? *(\\d+)%?");
    public static final Pattern chanceSnowPattern = Pattern.compile("[Cc]hance of (snow|flurries) *(around|near)? *(\\d+)%?");

    public final String title;
    public final String cast;
    public final String fullCast;

    public ForecastInfo(String title, int temp, int aveHum, String cond, String cast) {
        super(cond, temp, aveHum, title.toLowerCase().contains("night"));
        this.title = title;
        this.fullCast = cast;
        this.cast = shorten(cast);
    }

    public ForecastInfo(String title, String temp, int avehumidity, String cond, String cast) {
        this(title, Integer.parseInt(temp), avehumidity, cond, cast);
    }

    public String displayTemp(){
        return (night ? "↓" : "↑") + temp + "°";
    }

    public String getExtra(){
        Matcher m = chanceRainPattern.matcher(cast);
        if (m.find()){
            return "☂" + m.group(3);
        }

        m = chanceSnowPattern.matcher(cast);
        if (m.find()){
            return "☃" + m.group(3);
        }

        return hum + "%";
    }

    private String shorten(String cast) {
        int max = 60;

        cast = cast.replaceAll("with", "w/")
                .replaceAll("(followed by|will become)", "then")
                .replaceAll("(near|around) ", "~")
                .replaceAll("(\\d+) to (\\d+) mph", "$1-$2 mph")
                .replaceAll("at (\\d+-\\d+ mph)", "$1");
        cast = cast.length() > max ? cast.replaceAll("\\..*$", ".") : cast;

        if (cast.length() > max)
            cast = cast.replaceAll("(Low|high|low|High) (~|around |near )?(\\d+[Ff]?)\\. ?", "");

        if (cast.length() > max)
            cast = cast.substring(0, 48) + "...";

        return cast;
    }
}
