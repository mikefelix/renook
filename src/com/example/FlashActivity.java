package com.example;

import android.*;
import android.app.Activity;
import android.os.Bundle;

/**
 * FlashActivity
 * User: mikefelix
 * Date: 1/25/13
 */
public class FlashActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);

        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {

        }

        finish();
    }
}
