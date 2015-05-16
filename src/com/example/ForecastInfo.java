package com.example;

import android.util.Log;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
            return ":" + m.group(4) + "%";
        }

        m = chanceSnowPattern.matcher(cast);
        if (m.find()){
            return ":" + m.group(3) + "%";
        }

        return hum + "%";
    }

    @Contract("!null -> !null")
    private String shorten(String cast) {
        try {
            int max = 65;

            String shortCast = cast
//                    .replaceAll("with", "w/")
                    .replaceAll("((will be )?followed by|will become|becoming|(will give|giving) way to)", "then")
                    .replaceAll(" (and|then|with) ", ", ")
                    .replaceAll("(near|around|approaching) ", "~")
                    .replaceAll("afternoon", "PM")
                    .replaceAll("morning", "AM")
                    .replaceAll("overnight", "late")
                    .replaceAll("cloudiness", "clouds")
                    .replaceAll("through(out)?", "thru")
                    .replaceAll("(a |the )?possibility of", "possible")
//                    .replaceAll("possible\\.?", "poss.")
                    .replaceAll("(\\d+) to (\\d+) mph", "$1-$2 mph")
                    .replaceAll("at (\\d+-\\d+ mph)", "$1")
                    .replaceAll("Thunderstorm", "Storm")
                    .replaceAll("(shower or )?[Tt]hunderstorm", "storm")
                    .replaceAll("(\\w+) in the (PM|AM|afternoon|evening|night|morning)", "$2 $1")
    //                .replaceAll("([Pp])artly", "$1tly")
                    .replaceAll("([Ss])cattered", "$1catt.")
                    .replaceAll("\\.\\.", ".");

            shortCast = shortCast.replaceAll("(Low|high|low|High) (~|around |near )?(\\d+[Ff]?)\\. ?", "");

//        shortCast = shortCast.length() > max ? shortCast.replaceAll("\\..*$", ".") : shortCast;
// Partly to mostly cloudy with scattered showers and thunderstorms in the afternoon. High 87F. Winds SSE at 10 to 15 mph. Chance of rain 40%.
            while (shortCast.length() > max)
                shortCast = shortCast.replaceFirst("\\. +[^.]+\\.$", ".");

//        if (shortCast.length() > max)
//            shortCast = shortCast.substring(0, max - 3) + "...";

            Log.i("CLOCK", "Turned forecast \"" + cast + "\" into \"" + shortCast + "\"");
            return shortCast;
        }
        catch (Exception e) {
            Log.e("CLOCK", "Exception while shortening forecast", e);
            return cast;
        }
    }
}
