package com.example;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.widget.TextView;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClockActivity extends Activity {
    private MyTimer timer;
    private DateFormat timeFormat = new SimpleDateFormat("h:mm");
    private DateFormat hourFormat = new SimpleDateFormat("h");
    private DateFormat paddedHourFormat = new SimpleDateFormat(" h");
    private DateFormat minuteFormat = new SimpleDateFormat(":mm");
//    private DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");
    private DateFormat dateFormat = new SimpleDateFormat("EEE, 12/d");
    private PowerManager.WakeLock wakeLock;
    private int num = 0;
    private String temp;

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

    private String getTemp(){
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

        Pattern pattern = Pattern.compile(".*temp=\"([^\"]+)\".*");
        Matcher matcher = pattern.matcher(sb.toString());
        if (!matcher.matches())
            return "--";

        return matcher.group(1) + "°";
    }

    private void setViews(){
        TextView hour = (TextView) findViewById(R.id.hour);
        TextView minutes = (TextView) findViewById(R.id.minutes);
        TextView date = (TextView) findViewById(R.id.date);
        Date dateTime = new Date();

        if (num == 0)
            temp = getTemp();

        // Get the temp every 10 minutes
        num = (num + 1) % 10;

        String hourText = hourFormat.format(dateTime);
        if (hourText.length() == 1)
            hourText = " " + hourText;

        hour.setText(hourText);
        minutes.setText(minuteFormat.format(dateTime));
//        date.setText(dateFormat.format(dateTime) + " / " + getTemp());
        date.setText(dateFormat.format(dateTime) + "  ·  " + temp);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }

    private class MyTimer extends CountDownTimer {
        static final long period = 1000 * 60;

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
