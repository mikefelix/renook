package com.example;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QuoteActivity extends Activity {
    private final Quote[] quotes = new Quote[] {
            new Quote("The first principle is that you must not fool yourself and you are the easiest person to fool.", "Richard P. Feynman", 3f, 2f, "justify"),
            new Quote("It doesn't matter how beautiful your theory is, it doesn't matter how smart you are. If it doesn't agree with experiment, it's wrong.", "Richard P. Feynman", 3f, 2f, "justify"),
            new Quote("What can be asserted without proof can be dismissed without proof.", "Christopher Hitchens", 3.5f, 2f, "justify"),
    };

    private MyTimer timer;
    private int quoteNum = 0;
    PowerManager.WakeLock wakeLock;

    private static class Quote {
        private Quote(String text, String byline, float quoteSize, float bylineSize, String justify) {
            this.text = text;
            this.byline = byline;
            this.quoteSize = quoteSize;
            this.bylineSize = bylineSize;
            this.justify = justify;
        }

        String text, byline, justify;
        float quoteSize, bylineSize;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "renook");
        wakeLock.acquire();

        setupMainView();
//        setupWebView();
//        showWebQuote();
//        timer = new MyTimer();
//        timer.start();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }

    private void setupMainView() {
        setContentView(R.layout.main);
        findViewById(R.id.screen).setBackgroundColor(android.R.color.white);

        TextView quote = (TextView) findViewById(R.id.quote);
        TextView byline = (TextView) findViewById(R.id.byline);

        quote.setBackgroundColor(android.R.color.white);
        byline.setBackgroundColor(android.R.color.white);

//        quote.setText(quotes[quoteNum].text);
//        byline.setText(quotes[quoteNum].byline);
        quote.setText("________________________________");
        byline.setText("\n\n\n\n\n\n\n\n\n\n\n\n_____________________________________________");
    }

    private void setupWebView() {
        setContentView(R.layout.web);

        WebView view = new WebView(this);
        view.setId(R.id.quote);
        view.setVerticalScrollBarEnabled(false);

        LinearLayout layout = (LinearLayout) findViewById(R.id.screen);
        layout.addView(view);
        layout.setBackgroundColor(android.R.color.white);
    }

    private void showWebQuote() {
        Quote quote = quotes[quoteNum];
        WebView view = (WebView) findViewById(R.id.quote);
        view.loadData(getString(R.string.html).replaceAll("@quote@", quote.text)
                              .replaceAll("@byline@", quote.byline)
                              .replaceAll("@just@", quote.justify)
                              .replaceAll("@qsize@", "" + quote.quoteSize)
                              .replaceAll("@bsize@", "" + quote.bylineSize),
                      "text/html", "utf-8");

        quoteNum = (quoteNum + 1) % quotes.length;
    }

    private class MyTimer extends CountDownTimer {
        static final long period = 1000 * 60 * 30;

        public MyTimer() {
            super(period, period);
        }

        @Override public void onTick(long l) {
            showWebQuote();
        }

        @Override public void onFinish() {
            timer = new MyTimer();
            timer.start();
        }
    }
}
