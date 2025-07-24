package com.hospital.dietary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Schedule automatic order creation at 4:00 AM if not already scheduled
        SharedPreferences sharedPreferences = getSharedPreferences("DietaryAppPrefs", MODE_PRIVATE);
        boolean isScheduled = sharedPreferences.getBoolean("auto_order_scheduled", false);

        if (!isScheduled) {
            AutoOrderCreationService.scheduleAutoOrderCreation(this);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("auto_order_scheduled", true);
            editor.apply();
        }

        // Redirect to LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}