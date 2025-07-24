package com.hospital.dietary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutoOrderCreationReceiver extends BroadcastReceiver {

    private static final String TAG = "AutoOrderCreationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "AutoOrderCreationReceiver triggered");

        // Start the service to create default orders
        Intent serviceIntent = new Intent(context, AutoOrderCreationService.class);
        context.startService(serviceIntent);
    }
}