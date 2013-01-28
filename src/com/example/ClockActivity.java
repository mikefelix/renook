package com.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClockActivity extends Activity {
    private MyTimer timer;
    private DateFormat timeFormat = new SimpleDateFormat("h:mm");
    private DateFormat hourFormat = new SimpleDateFormat("h");
    private DateFormat hour24Format = new SimpleDateFormat("H");
    private DateFormat paddedHourFormat = new SimpleDateFormat(" h");
    private DateFormat minuteFormat = new SimpleDateFormat(":mm");
//    private DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");
    private DateFormat dateFormat = new SimpleDateFormat("EEE M/d");
    private PowerManager.WakeLock wakeLock;
    private int getWeatherCounter = 0;
    private int refreshCounter = 0;
    private String currTemp;
    private int currIcon = R.drawable.clear;
    private int foreIcon1 = R.drawable.clear;
    private int foreIcon2 = R.drawable.clear;
    private Pattern currWeatherPattern = Pattern.compile("<yweather:condition ([^>]*)/>");
    private Pattern forecastWeatherPattern = Pattern.compile("<yweather:forecast ([^>]*)/>");
    private Pattern codePattern = Pattern.compile("code=\"([^\"]*)\"");
    private Pattern tempPattern = Pattern.compile("temp=\"([^\"]*)\"");
    private Pattern highPattern = Pattern.compile("high=\"([^\"]*)\"");
    private Pattern lowPattern = Pattern.compile("low=\"([^\"]*)\"");

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clock);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "renook");
        wakeLock.acquire();

        setViews();

        timer = new MyTimer();
        timer.start();
    }

    private void setViews(){
        TextView hour = (TextView) findViewById(R.id.hour);
        TextView shortHour = (TextView) findViewById(R.id.shortHour);
        TextView minutes = (TextView) findViewById(R.id.minutes);
        TextView date = (TextView) findViewById(R.id.date);
        TextView temp = (TextView) findViewById(R.id.temp);
        ImageView icon = (ImageView) findViewById(R.id.icon);
        ImageView icon2 = (ImageView) findViewById(R.id.icon2);
        ImageView icon3 = (ImageView) findViewById(R.id.icon3);

        Date dateTime = new Date();

        if (getWeatherCounter == 0)
            refreshWeather(Integer.parseInt(hour24Format.format(dateTime)));

        // Get the temp every 10 minutes
        getWeatherCounter = (getWeatherCounter + 1) % 60;

        String hourText = hourFormat.format(dateTime);
        shortHour.setVisibility(hourText.length() == 2 ? View.GONE : View.VISIBLE);
        hour.setVisibility(hourText.length() == 1 ? View.GONE : View.VISIBLE);

        hour.setText(hourText);
        shortHour.setText(hourText);
        minutes.setText(minuteFormat.format(dateTime));
        date.setText(dateFormat.format(dateTime));
        temp.setText(currTemp);
        icon.setImageResource(currIcon);
        icon2.setImageResource(foreIcon1);
        icon3.setImageResource(foreIcon2);

        if (refreshCounter == 0){
            startActivity(new Intent(this, FlashActivity.class));
        }

        refreshCounter = (refreshCounter + 1) % 360;
    }

    private void refreshWeather(int hourNum) {
        URL url;
        BufferedReader reader = null;
        String line;
        StringBuilder sb = new StringBuilder();

        try {
            url = new URL("http://weather.yahooapis.com/forecastrss?w=12794144");
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (MalformedURLException mue) {
             mue.printStackTrace();
        }
        catch (IOException ioe) {
             ioe.printStackTrace();
        }
        finally {
            try {
                if (reader != null)
                    reader.close();
            }
            catch (IOException ioe) {
                // nothing to see here
            }
        }

        String str = sb.toString();

        currTemp = "--";
        currIcon = foreIcon1 = foreIcon2 = R.drawable.clear;

        Matcher currMatcher = currWeatherPattern.matcher(str);
        if (currMatcher.find()){
            Matcher tempMatcher = tempPattern.matcher(currMatcher.group(1));
            if (tempMatcher.find())
                currTemp = tempMatcher.group(1) + "°";

            Matcher codeMatcher = codePattern.matcher(currMatcher.group(1));
            if (codeMatcher.find())
                currIcon = getIcon(codeMatcher.group(1), hourNum);

            Matcher forecastMatcher = forecastWeatherPattern.matcher(str);
            if (forecastMatcher.find()){
                codeMatcher = codePattern.matcher(forecastMatcher.group(1));
                if (codeMatcher.find())
                    foreIcon1 = getIcon(codeMatcher.group(1), (hourNum + 12) % 24);
            }

            if (forecastMatcher.find()){
                codeMatcher = codePattern.matcher(forecastMatcher.group(1));
                if (codeMatcher.find())
                    foreIcon2 = getIcon(codeMatcher.group(1), hourNum);
            }

            if (hourNum > 17){
                foreIcon1 = foreIcon2;
                foreIcon2 = R.drawable.clear;
            }

        }
    }

    private int getIcon(String num, int hour) {
        switch (Integer.parseInt(num)){
            case 3:	 // severe thunderstorms
            case 4:	 // thunderstorms
            case 45: //	thundershowers
                return R.drawable.tstorm;

            case 37: //	isolated thunderstorms
            case 47: //	isolated thundershowers
            case 38: //	scattered thunderstorms
            case 39: //	scattered thunderstorms
                return R.drawable.tstorm1;

            case 14: //	light snow showers
            case 42: //	scattered snow showers
            case 13: //	snow flurries
                return R.drawable.lightsnow;

            case 5:	 // mixed rain and snow
            case 7:	 // mixed snow and sleet
            case 16: //	snow
            case 41: //	heavy snow
            case 43: //	heavy snow
            case 46: //	snow showers
            case 15: //	blowing snow
                return R.drawable.snow;

            case 17: //	hail
            case 35: //	mixed rain and hail
                return R.drawable.hail;

            case 8:	 // freezing drizzle
            case 9:	 // drizzle
            case 40: //	scattered showers
                return R.drawable.drizzle;

            case 6:	 // mixed rain and sleet
            case 10: //	freezing rain
            case 11: //	showers
            case 12: //	showers
            case 18: //	sleet
                return R.drawable.rain;

            case 19: //	dust
            case 21: //	haze
            case 22: //	smoky
                return R.drawable.hazy;

            case 0:	 // tornado
            case 1:	 // tropical storm
            case 2:	 // hurricane
            case 23: //	blustery
            case 24: //	windy
                return R.drawable.windy;

            case 25: //	cold
                return R.drawable.snowflake;
            case 36: //	hot
                return R.drawable.hot;

            case 20: //	foggy
                return hour < 6 || hour > 20 ? R.drawable.fogmoon : R.drawable.fogsun;

            case 29: //	partly cloudy (night)
                return R.drawable.cloudmoon;

            case 30: //	partly cloudy (day)
                return R.drawable.cloudsun;

            case 26: //	cloudy
            case 44: //	partly cloudy
                return hour < 6 || hour > 20 ? R.drawable.cloudmoon : R.drawable.cloudsun;

            case 27: //	mostly cloudy (night)
            case 28: //	mostly cloudy (day)
                return R.drawable.cloudy;

            case 31: //	clear (night)
            case 33: //	fair (night)
                return R.drawable.moon;

            case 32: //	sunny
            case 34: //	fair (day)
                return R.drawable.sun;

            default:
                return R.drawable.na;
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }

    private class MyTimer extends CountDownTimer {
        static final long period = 1000 * 10;

        public MyTimer() {
            super(period, period);
        }

        @Override public void onTick(long l) {
            setViews();
        }

        @Override public void onFinish() {
            timer = new MyTimer();
            timer.start();
        }
    }
}
