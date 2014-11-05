package com.example;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
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

    private final DateFormat timeFormat = new SimpleDateFormat("h:mm");
    private final DateFormat hourFormat = new SimpleDateFormat("h");
    private final DateFormat minuteFormat = new SimpleDateFormat(":mm");
    private final DateFormat dateFormat = new SimpleDateFormat("M/d");

    private MyTimer timer;
    private PowerManager.WakeLock wakeLock;
    private ForecastInfo forecast1 = new ForecastInfo("Starting up...", 100, 40, "blank", "Starting up...");
    private ForecastInfo forecast2 = new ForecastInfo("Starting up...", 100, 40, "blank", "Starting up...");
    private WeatherInfo current = new WeatherInfo("Starting up...", 108, 58, false);
    private Date dateTime = new Date();

    private ForecastInfo failedForecast = new ForecastInfo("Refresh failed", 100, 40, "blank", "Error retrieving forecast.");
    private WeatherInfo failedCurrent = new WeatherInfo("Refresh failed", 0, 0, false);
    private WeatherInfo failedTwiceCurrent = new WeatherInfo("Refresh failed", 0, 0, false);
    private PendingIntent restartIntent;

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

        refreshViews();

        restartIntent = PendingIntent.getActivity(this.getBaseContext(), 0, new Intent(getIntent()), getIntent().getFlags());

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable throwable) {
                try {
                    File logFile = new File(Environment.getExternalStorageDirectory(), "exception_log.txt");
                    PrintWriter writer = new PrintWriter(new FileWriter(logFile));
                    writer.write(throwable.getMessage());
                    writer.write(throwable.toString());
                }
                catch (IOException e) {
                }

                AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, restartIntent);
                System.exit(2);
            }
        });

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

    @Override protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }

    private void refreshViews(){
        TextView hourView = (TextView) findViewById(R.id.hour);
        TextView shortHourView = (TextView) findViewById(R.id.shortHour);
        TextView minutesView = (TextView) findViewById(R.id.minutes);
        TextView dateView = (TextView) findViewById(R.id.date);
        TextView tempView = (TextView) findViewById(R.id.temp);
        TextView temp1View = (TextView) findViewById(R.id.high1);
        TextView extra1View = (TextView) findViewById(R.id.low1);
        TextView temp2View = (TextView) findViewById(R.id.high2);
        TextView extra2View = (TextView) findViewById(R.id.low2);
        TextView cast1View = (TextView) findViewById(R.id.cast1);
        TextView cast2View = (TextView) findViewById(R.id.cast2);
        ImageView currIconView = (ImageView) findViewById(R.id.currIcon);
        ImageView foreIcon1View = (ImageView) findViewById(R.id.foreIcon1);
        ImageView foreIcon2View = (ImageView) findViewById(R.id.foreIcon2);
        TextView title1View = (TextView) findViewById(R.id.period1);
        TextView title2View = (TextView) findViewById(R.id.period2);
        TextView humView = (TextView) findViewById(R.id.hum);

        String hourText = hourFormat.format(dateTime);
        shortHourView.setVisibility(hourText.length() == 2 ? View.GONE : View.VISIBLE);
        hourView.setVisibility(hourText.length() == 1 ? View.GONE : View.VISIBLE);

        hourView.setText(hourText);
        shortHourView.setText(hourText);
        minutesView.setText(minuteFormat.format(dateTime));
        dateView.setText(dateFormat.format(dateTime));

        if (current != failedCurrent) {
            tempView.setText(current == failedTwiceCurrent ? "--" : current.displayTemp());
            humView.setText(current == failedTwiceCurrent ? "--" : current.hum);
        }

        temp1View.setText(forecast1.displayTemp());
        temp2View.setText(forecast2.displayTemp());
        extra1View.setText(forecast1.getExtra());
        extra2View.setText(forecast2.getExtra());
        cast1View.setText(forecast1.cast);
        cast2View.setText(forecast2.cast);
        title1View.setText(forecast1.title.toUpperCase());
        title2View.setText(forecast2.title.toUpperCase());

        currIconView.setImageResource(current.getIcon());
        foreIcon1View.setImageResource(forecast1.getIcon());
        foreIcon2View.setImageResource(forecast2.getIcon());
    }

    private void refreshForecast() {
        Log.i("CLOCK", "Refreshing forecast at " + timeFormat.format(new Date()));
        String text = getUrl(FORE_URL);
        if (text == null || "".equals(text)) {
            Log.e("CLOCK", "Returning early because no text received.");
            forecast1 = failedForecast;
            forecast2 = failedForecast;
            return;
        }

        Gson gson = new Gson();
        Forecast forecast = gson.fromJson(text, Forecast.class);
        Forecast.SimpleForecast simple = forecast.forecast.simpleforecast;
        Forecast.TextForecast textual = forecast.forecast.txt_forecast;
        Forecast.SimpleForecastDay today = simple.forecastday[0];
        Forecast.SimpleForecastDay tomorrow = simple.forecastday[1];
        Forecast.TextForecastDay todayText = textual.forecastday[0];
        Forecast.TextForecastDay tonightText = textual.forecastday[1];
        Forecast.TextForecastDay tomorrowText = textual.forecastday[2];

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 16) { // show today and tonight
            forecast1 = new ForecastInfo(todayText.title, today.high.fahrenheit, today.avehumidity, todayText.icon, todayText.fcttext);
            forecast2 = new ForecastInfo(tonightText.title, tomorrow.low.fahrenheit,today.avehumidity,  tonightText.icon, tonightText.fcttext);
        }
        else { // show tonight and tomorrow
            forecast1 = new ForecastInfo(tonightText.title, today.low.fahrenheit, today.avehumidity, tonightText.icon, tonightText.fcttext);
            forecast2 = new ForecastInfo(tomorrowText.title, tomorrow.high.fahrenheit, tomorrow.avehumidity, tomorrowText.icon, tomorrowText.fcttext);
        }
    }

    private void refreshWeather() {
        Log.i("CLOCK", "Refreshing current at " + timeFormat.format(new Date()));
        String text = getUrl(CURR_URL);
        if (text == null || "".equals(text)) {
            Log.e("CLOCK", "Returning early because no text received.");
            current = (current == failedCurrent || current == failedTwiceCurrent) ? failedTwiceCurrent : failedCurrent;
            return;
        }

        Gson gson = new Gson();
        Conditions conditions = gson.fromJson(text, Conditions.class);

        current = new WeatherInfo(conditions.current_observation.icon,
                Math.round(Float.parseFloat(conditions.current_observation.temp_f)),
                Integer.parseInt(conditions.current_observation.relative_humidity.replaceAll("[^0-9.]", "")),
                isNight(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
    }

    private boolean isNight(int hour){
        return hour < 7 || hour > 19;
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
//            currTemp = "!!";
//            currIcon = foreIcon1 = foreIcon2 = R.drawable.blank;
            return null;
        }
        catch (IOException ioe) {
            Log.e("CLOCK", "I/O Exception in request: " + ioe.getMessage());
//            currTemp = currTemp.replaceAll("[^0-9]", "?");
//            currIcon = foreIcon1 = foreIcon2 = R.drawable.blank;
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

    private int getWeatherCounter = 0;
    private int flashCounter = 0;
    private int lastForecastQuarter = -1;

    private class MyTimer extends CountDownTimer {
        public static final long REFRESH_PERIOD = 1000 * 10;

        public MyTimer() {
            super(REFRESH_PERIOD, REFRESH_PERIOD);
        }

        @Override public void onTick(long l) {
            Calendar cal = Calendar.getInstance();
            dateTime = cal.getTime();

            refreshViews();

            if (getWeatherCounter == 0) {
                new Thread(){
                    public void run() {
                        try {
                            refreshWeather();
                        }
                        catch (Exception e) {
                            Log.e("CLOCK", "Error refreshing current: " + e.getClass().getName() + "; " + e.getMessage());
                        }
                    }
                }.start();
            }

            // Get the current conditions every 5 minutes and the forecast every fifteen.
            getWeatherCounter = (getWeatherCounter + 1) % 30;

            int min = cal.get(Calendar.MINUTE);
            int quarter = min / 15;
            if (lastForecastQuarter != quarter) {
                lastForecastQuarter = quarter;
                new Thread(){
                    public void run() {
                        try {
                            refreshForecast();
                        }
                        catch (Exception e) {
                            Log.e("CLOCK", "Error refreshing forecast: " + e.getClass().getName() + "; " + e.getMessage());
                        }
                    }
                }.start();
            }

            if (flashCounter == 0)
                startActivity(new Intent(ClockActivity.this, FlashActivity.class));

            flashCounter = (flashCounter + 1) % 360;
        }

        @Override public void onFinish() {
            timer = new MyTimer();
            timer.start();
        }
    }
}
