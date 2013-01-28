package com.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * StartAtBootReceiver
 * User: mikefelix
 * Date: 1/22/13
 */
public class StartAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent intentYeah = new Intent(context, ClockActivity.class);
            context.startActivity(intentYeah);
        }
    }
}