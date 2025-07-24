package com.hospital.dietary;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AutoOrderCreationService extends Service {

    private static final String TAG = "AutoOrderCreationService";
    private static final String CHANNEL_ID = "dietary_auto_order_channel";
    private static final int NOTIFICATION_ID = 1001;

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "AutoOrderCreationService started");

        // Run the auto-creation in a background thread
        new Thread(() -> {
            try {
                createDefaultOrdersForActivePatients();
                stopSelf(); // Stop the service after completion
            } catch (Exception e) {
                Log.e(TAG, "Error creating default orders", e);
                stopSelf();
            }
        }).start();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createDefaultOrdersForActivePatients() {
        try {
            List<Patient> allPatients = patientDAO.getAllPatients();
            int createdCount = 0;
            int pendingCount = 0;

            // Get current day of week for default menu selection
            Calendar calendar = Calendar.getInstance();
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            for (Patient patient : allPatients) {
                // Skip discharged patients
                if (patient.isDischarged()) {
                    continue;
                }

                // Check if patient already has today's orders
                // This prevents duplicate creation if service runs multiple times
                Calendar patientOrderDate = Calendar.getInstance();
                patientOrderDate.setTime(patient.getOrderDate() != null ?
                        patient.getOrderDate() : new Date(0));

                Calendar today = Calendar.getInstance();

                boolean sameDay = patientOrderDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        patientOrderDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);

                if (!sameDay) {
                    // Create default order for today
                    createDefaultOrderForPatient(patient, dayOfWeek);
                    createdCount++;
                } else {
                    // Patient already has today's order - check if it's pending
                    if (!patient.isBreakfastComplete() && !patient.isBreakfastNPO() ||
                            !patient.isLunchComplete() && !patient.isLunchNPO() ||
                            !patient.isDinnerComplete() && !patient.isDinnerNPO()) {
                        pendingCount++;
                    }
                }
            }

            // Show notification with results
            if (createdCount > 0 || pendingCount > 0) {
                showNotification(createdCount, pendingCount);
            }

            Log.d(TAG, "Created default orders for " + createdCount + " patients");
            Log.d(TAG, "Found " + pendingCount + " patients with pending orders");

        } catch (Exception e) {
            Log.e(TAG, "Error in createDefaultOrdersForActivePatients", e);
        }
    }

    private void createDefaultOrderForPatient(Patient patient, int dayOfWeek) {
        try {
            // Reset meal completion flags
            patient.setBreakfastComplete(false);
            patient.setLunchComplete(false);
            patient.setDinnerComplete(false);
            patient.setBreakfastNPO(false);
            patient.setLunchNPO(false);
            patient.setDinnerNPO(false);

            // Set order date to today
            patient.setOrderDate(new Date());

            // Keep existing diet information and modifications
            // The diet type, texture modifications, liquid thickness, etc.
            // are all carried forward from previous day

            // Clear previous meal selections but keep diet type
            patient.setBreakfastMain("");
            patient.setBreakfastSide("");
            patient.setBreakfastDrink("");
            patient.setBreakfastJuices("");

            patient.setLunchMain("");
            patient.setLunchSide("");
            patient.setLunchDrink("");
            patient.setLunchJuices("");

            patient.setDinnerMain("");
            patient.setDinnerSide("");
            patient.setDinnerDrink("");
            patient.setDinnerJuices("");

            // Individual meal diets default to main diet if not set
            if (patient.getBreakfastDiet() == null) {
                patient.setBreakfastDiet(patient.getDiet());
            }
            if (patient.getLunchDiet() == null) {
                patient.setLunchDiet(patient.getDiet());
            }
            if (patient.getDinnerDiet() == null) {
                patient.setDinnerDiet(patient.getDiet());
            }

            // Update patient in database
            patientDAO.updatePatient(patient);

            Log.d(TAG, "Created default order for patient: " +
                    patient.getPatientFirstName() + " " + patient.getPatientLastName());

        } catch (Exception e) {
            Log.e(TAG, "Error creating default order for patient ID: " +
                    patient.getPatientId(), e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Dietary Auto Orders";
            String description = "Notifications for automatic order creation";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(int createdCount, int pendingCount) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create intent for pending orders activity
        Intent intent = new Intent(this, PendingOrdersActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String message;
        if (createdCount > 0 && pendingCount > 0) {
            message = String.format("Created %d new orders. %d orders pending.",
                    createdCount, pendingCount);
        } else if (createdCount > 0) {
            message = String.format("Created %d new patient orders for today.", createdCount);
        } else {
            message = String.format("%d patient orders pending for today.", pendingCount);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_today)
                .setContentTitle("Daily Orders Created")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Static method to schedule the service to run at 4:00 AM daily
    public static void scheduleAutoOrderCreation(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AutoOrderCreationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to start at 4:00 AM
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If it's already past 4:00 AM, set for next day
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Set repeating alarm for every 24 hours
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent);

        Log.d(TAG, "Scheduled auto order creation for 4:00 AM daily");
    }

    // Static method to cancel the scheduled service
    public static void cancelAutoOrderCreation(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AutoOrderCreationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        alarmManager.cancel(pendingIntent);

        Log.d(TAG, "Cancelled auto order creation schedule");
    }
}