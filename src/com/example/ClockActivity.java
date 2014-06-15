package com.example;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;

import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ClockActivity extends Activity {
    public static final String CURR_URL = "http://api.wunderground.com/api/4ff057a10c9613a4/conditions/q/UT/Murray.json";
    public static final String FORE_URL = "http://api.wunderground.com/api/4ff057a10c9613a4/forecast/q/UT/Murray.json";

    private MyTimer timer;
    private DateFormat timeFormat = new SimpleDateFormat("h:mm");
    private DateFormat hourFormat = new SimpleDateFormat("h");
    private DateFormat hour24Format = new SimpleDateFormat("H");
    private DateFormat minuteFormat = new SimpleDateFormat(":mm");
    private DateFormat dateFormat = new SimpleDateFormat("MMM d");
    private PowerManager.WakeLock wakeLock;
    private int getWeatherCounter = 0;
    private int getForecastCounter = 0;
    private int refreshCounter = 0;
    private String currTemp = "108°", highTemp1 = "108°", lowTemp1 = "108°", highTemp2 = "108°", lowTemp2 = "108°";
    private String cast1, cast2;
    private String cond1 = "conditions", cond2 = "conditions";
    private int currIcon = R.drawable.clear;
    private int foreIcon1 = R.drawable.clear;
    private int foreIcon2 = R.drawable.clear;
    private int lastWeatherReqNum = 0;
    private int lastForecastReqNum = 0;
    boolean continuing = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clockp);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "renook");
        wakeLock.acquire();

        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setViews();

        timer = new MyTimer();
        timer.start();
    }

    @Override
    protected void onResume() {
        wakeLock.acquire();
        super.onResume();
    }

    @Override
    protected void onRestart() {
        wakeLock.acquire();
        super.onResume();
    }

    @Override
    protected void onPause() {
        wakeLock.release();
        super.onPause();
    }

    @Override
    protected void onStop() {
        wakeLock.release();
        super.onStop();
    }

    private void setViews(){
        TextView hourView = (TextView) findViewById(R.id.hour);
        TextView shortHourView = (TextView) findViewById(R.id.shortHour);
        TextView minutesView = (TextView) findViewById(R.id.minutes);
        TextView dateView = (TextView) findViewById(R.id.date);
        TextView tempView = (TextView) findViewById(R.id.temp);
        TextView high1View = (TextView) findViewById(R.id.high1);
        TextView low1View = (TextView) findViewById(R.id.low1);
        TextView high2View = (TextView) findViewById(R.id.high2);
        TextView low2View = (TextView) findViewById(R.id.low2);
        TextView cast1View = (TextView) findViewById(R.id.cast1);
        TextView cast2View = (TextView) findViewById(R.id.cast2);
//        TextView cond1View = (TextView) findViewById(R.id.cond1);
//        TextView cond2View = (TextView) findViewById(R.id.cond2);
        ImageView currIconView = (ImageView) findViewById(R.id.currIcon);
        ImageView foreIcon1View = (ImageView) findViewById(R.id.foreIcon1);
        ImageView foreIcon2View = (ImageView) findViewById(R.id.foreIcon2);

        final Date dateTime = new Date();

        if (getWeatherCounter == 0) {
            lastWeatherReqNum++;
            new Thread(){
                public void run() {
                    refreshWeather(lastWeatherReqNum, Integer.parseInt(hour24Format.format(dateTime)));
                }
            }.start();
        }

        if (getForecastCounter == 0) {
            lastForecastReqNum++;
            new Thread(){
                public void run() {
                    refreshForecast(lastForecastReqNum, Integer.parseInt(hour24Format.format(dateTime)));
                }
            }.start();
        }

        // Get the temp every 3 minutes
        getWeatherCounter = (getWeatherCounter + 1) % 18;
        getForecastCounter = (getForecastCounter + 1) % 360;

        String hourText = hourFormat.format(dateTime);
        shortHourView.setVisibility(hourText.length() == 2 ? View.GONE : View.VISIBLE);
        hourView.setVisibility(hourText.length() == 1 ? View.GONE : View.VISIBLE);

        hourView.setText(hourText);
        shortHourView.setText(hourText);
        minutesView.setText(minuteFormat.format(dateTime));
        dateView.setText(dateFormat.format(dateTime));
        tempView.setText(currTemp);
        high1View.setText(highTemp1);
        low1View.setText(lowTemp1);
        high2View.setText(highTemp2);
        low2View.setText(lowTemp2);
        cast1View.setText(cast1);
        cast2View.setText(cast2);
//        cond1View.setText(cond1);
//        cond2View.setText(cond2);

        currIconView.setImageResource(currIcon);
        foreIcon1View.setImageResource(foreIcon1);
        foreIcon2View.setImageResource(foreIcon2);

        if (refreshCounter == 0){
            startActivity(new Intent(this, FlashActivity.class));
        }

        refreshCounter = (refreshCounter + 1) % 360;
        if (!continuing)
            finish();
    }

    private String shortenForecast(String cast) {
        cast = cast.replaceAll("with", "w/")
                .replaceAll("(followed by|will become)", "then")
                .replaceAll("(near|around) ", "~")
                .replaceAll("(\\d+) to (\\d+) mph", "$1-$2 mph");
        cast = cast.length() > 50 ? cast.replaceAll("\\..*$", ".") : cast;

        if (cast.length() > 50)
            cast = cast.replaceAll("(Low|high|low|High) ~?(\\d+[Ff]?)\\. ?", "");

        if (cast.length() > 50)
            cast = cast.substring(0, 48) + "...";

        return cast;
    }

    private String shortenDay(String date) {
        return date
                .replace("Sat", "Sa")
                .replace("Sun", "Su")
                .replace("Mon", "M")
                .replace("Tue", "T")
                .replace("Wed", "W")
                .replace("Thu", "Th")
                .replace("Fri", "F");
    }

    private void refreshForecast(int reqNum, int hourNum) {
        Log.i("CLOCK", "Refreshing forecast with number " + reqNum + " at " + currentTime());

        if (lastWeatherReqNum != reqNum) {
            Log.e("CLOCK", "Request " + reqNum + " attempting processing but current req is " + lastWeatherReqNum);
            return;
        }

        String text = getUrl(FORE_URL);
        if (text == null)
            return;

        Gson gson = new Gson();
        Forecast forecast = gson.fromJson(text, Forecast.class);

        Forecast.SimpleForecast f = forecast.forecast.simpleforecast;
        Forecast.TextForecast t = forecast.forecast.txt_forecast;

        Forecast.SimpleForecastDay period1 = f.forecastday[1];
        Forecast.SimpleForecastDay period2 = f.forecastday[2];
        Forecast.TextForecastDay textPeriod1 = t.forecastday[1];
        Forecast.TextForecastDay textPeriod2 = t.forecastday[2];

        String title1 = textPeriod1.title.toUpperCase();
        String title2 = textPeriod2.title.toUpperCase();

        ((TextView)findViewById(R.id.period1)).setText(title1);
        ((TextView)findViewById(R.id.period2)).setText(title2);

        lowTemp1 = period1.low.fahrenheit + "°";
        highTemp1 = period1.high.fahrenheit + "°";

        lowTemp2 = period2.low.fahrenheit + "°";
        highTemp2 = period2.high.fahrenheit + "°";

        cast1 = shortenForecast(textPeriod1.fcttext);
        cast2 = shortenForecast(textPeriod2.fcttext);

        foreIcon1 = getIcon(period1.icon, title1.contains("NIGHT"));
        foreIcon2 = getIcon(period2.icon, title2.contains("NIGHT"));
    }

    private boolean isNight(int hour){
        return hour < 8 || hour > 19;
    }

    private void refreshWeather(int reqNum, int hourNum) {
        Log.i("CLOCK", "Refreshing weather with number " + reqNum + " at " + currentTime());

        if (lastWeatherReqNum != reqNum) {
            Log.e("CLOCK", "Request " + reqNum + " attempting processing but current req is " + lastWeatherReqNum);
            return;
        }

        String text = getUrl(CURR_URL);
        if (text == null)
            return;

        Gson gson = new Gson();
        Conditions conditions = gson.fromJson(text, Conditions.class);

        int rounded = Math.round(Float.parseFloat(conditions.current_observation.temp_f));
        currTemp = rounded + "°";
        currIcon = getIcon(conditions.current_observation.icon, isNight(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
    }

    private String getUrl(String urlStr){
        URL url = null;
        BufferedReader reader = null;
        String line;
        StringBuilder sb = new StringBuilder();

        try {
            url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(20000);
            for (int i = 0; i < 5; i++) {
                try {
                    InputStream is = connection.getInputStream();
                    InputStreamReader in = new InputStreamReader(is);
                    reader = new BufferedReader(in);
                    break;
                }
                catch (SocketTimeoutException e) {
                    //continue;
//                    Log.e("CLOCK", "Connection failed in " + reqNum + ", retrying.");
                }
            }

            if (reader == null)
                throw new IOException("Read timed out after five tries.");

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        catch (MalformedURLException mue) {
            Log.e("CLOCK", "Malformed URL: " + url);
            currTemp = "!!";
            currIcon = foreIcon1 = foreIcon2 = R.drawable.blank;
            return null;
        }
        catch (IOException ioe) {
//            Log.e("CLOCK", "I/O Exception in weather thread " + reqNum + " at " + currentTime() + ": " + ioe.getMessage());
            currTemp = currTemp.replaceAll("[^0-9]", "?");
            currIcon = foreIcon1 = foreIcon2 = R.drawable.blank;
            return null;
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

        return sb.toString();
    }

    private String currentTime() {
        return timeFormat.format(Calendar.getInstance().getTime());
    }

    private int getIcon(String name, boolean night) {
        if ("chanceflurries".equals(name))
            return night ? R.drawable.nt_chanceflurries : R.drawable.chanceflurries;
        else if ("chancerain".equals(name)) 
            return night ? R.drawable.nt_chancerain : R.drawable.chancerain;
        else if ("chancesleet".equals(name)) 
            return night ? R.drawable.nt_chancesleet : R.drawable.chancesleet;
        else if ("chancesnow".equals(name)) 
            return night ? R.drawable.nt_chancesnow : R.drawable.chancesnow;
        else if ("chancetstorms".equals(name)) 
            return night ? R.drawable.nt_chancetstorms : R.drawable.chancetstorms;
        else if ("cloudy".equals(name)) 
            return night ? R.drawable.nt_cloudy : R.drawable.cloudy;
        else if ("flurries".equals(name)) 
            return night ? R.drawable.nt_flurries : R.drawable.flurries;
        else if ("fog".equals(name)) 
            return night ? R.drawable.nt_fog : R.drawable.fog;
        else if ("hazy".equals(name)) 
            return night ? R.drawable.nt_hazy : R.drawable.hazy;
        else if ("rain".equals(name)) 
            return night ? R.drawable.nt_rain : R.drawable.rain;
        else if ("snow".equals(name)) 
            return night ? R.drawable.nt_snow : R.drawable.snow;
        else if ("sunny".equals(name)) 
            return night ? R.drawable.nt_sunny : R.drawable.sunny;
        else if ("sleet".equals(name)) 
            return night ? R.drawable.nt_sleet : R.drawable.sleet;
        else if ("tstorms".equals(name)) 
            return night ? R.drawable.nt_tstorms : R.drawable.tstorms;
        else if ("mostlycloudy".equals(name)) 
            return night ? R.drawable.nt_mostlycloudy : R.drawable.mostlycloudy;
        else if ("partlycloudy".equals(name)) 
            return night ? R.drawable.nt_partlycloudy : R.drawable.partlycloudy;
        else if ("mostlysunny".equals(name)) 
            return night ? R.drawable.nt_mostlysunny : R.drawable.mostlysunny;
        else if ("partlysunny".equals(name)) 
            return night ? R.drawable.nt_partlysunny : R.drawable.partlysunny;
        else 
            return night ? R.drawable.nt_clear : R.drawable.clear;
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
