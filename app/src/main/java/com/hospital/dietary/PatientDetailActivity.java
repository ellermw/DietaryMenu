package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

public class PatientDetailActivity extends AppCompatActivity {

    private static final String TAG = "PatientDetailActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    private int patientId;
    private Patient currentPatient;

    // UI Components - Patient Information
    private TextView patientNameText;
    private TextView locationText;
    private TextView dietText;
    private TextView fluidRestrictionText;
    private TextView textureModificationsText;
    private TextView liquidThicknessText;
    private TextView createdDateText;

    // UI Components - Meal Status
    private LinearLayout mealStatusSection;
    private LinearLayout breakfastSection;
    private LinearLayout lunchSection;
    private LinearLayout dinnerSection;

    private TextView breakfastDietText;
    private TextView lunchDietText;
    private TextView dinnerDietText;

    private TextView breakfastMenuText;
    private TextView lunchMenuText;
    private TextView dinnerMenuText;

    private CheckBox breakfastCompleteCheckBox;
    private CheckBox lunchCompleteCheckBox;
    private CheckBox dinnerCompleteCheckBox;

    private CheckBox breakfastNPOCheckBox;
    private CheckBox lunchNPOCheckBox;
    private CheckBox dinnerNPOCheckBox;

    // UI Components - Action Buttons
    private Button editPatientButton;
    private Button editMealPlanButton;
    private Button transferPatientButton;
    private Button dischargePatientButton;
    private Button markAllCompleteButton;
    private Button markAllNPOButton;

    // Wing-Room mapping
    private Map<String, String[]> wingRoomMap = new HashMap<>();
    private String[] wings = {"1 North", "1 South", "2 North", "2 South", "3 East", "3 West"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        // Get patient ID from intent
        patientId = getIntent().getIntExtra("patient_id", -1);
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        if (patientId == -1) {
            Toast.makeText(this, "Invalid patient ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Patient Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeWingRoomMap();
        initializeUI();
        setupListeners();
        loadPatientData();
    }

    private void initializeWingRoomMap() {
        wingRoomMap.put("1 North", new String[]{"101", "102", "103", "104", "105", "106", "107", "108", "109", "110"});
        wingRoomMap.put("1 South", new String[]{"111", "112", "113", "114", "115", "116", "117", "118", "119", "120"});
        wingRoomMap.put("2 North", new String[]{"201", "202", "203", "204", "205", "206", "207", "208", "209", "210"});
        wingRoomMap.put("2 South", new String[]{"211", "212", "213", "214", "215", "216", "217", "218", "219", "220"});
        wingRoomMap.put("3 East", new String[]{"301", "302", "303", "304", "305", "306", "307", "308", "309", "310"});
        wingRoomMap.put("3 West", new String[]{"311", "312", "313", "314", "315", "316", "317", "318", "319", "320"});
    }

    private void initializeUI() {
        // Patient Information
        patientNameText = findViewById(R.id.patientNameText);
        locationText = findViewById(R.id.locationText);
        dietText = findViewById(R.id.dietText);
        fluidRestrictionText = findViewById(R.id.fluidRestrictionText);
        textureModificationsText = findViewById(R.id.textureModificationsText);
        liquidThicknessText = findViewById(R.id.liquidThicknessText);
        createdDateText = findViewById(R.id.createdDateText);

        // Meal Status Section
        mealStatusSection = findViewById(R.id.mealStatusSection);
        breakfastSection = findViewById(R.id.breakfastSection);
        lunchSection = findViewById(R.id.lunchSection);
        dinnerSection = findViewById(R.id.dinnerSection);

        breakfastDietText = findViewById(R.id.breakfastDietText);
        lunchDietText = findViewById(R.id.lunchDietText);
        dinnerDietText = findViewById(R.id.dinnerDietText);

        breakfastMenuText = findViewById(R.id.breakfastMenuText);
        lunchMenuText = findViewById(R.id.lunchMenuText);
        dinnerMenuText = findViewById(R.id.dinnerMenuText);

        breakfastCompleteCheckBox = findViewById(R.id.breakfastCompleteCheckBox);
        lunchCompleteCheckBox = findViewById(R.id.lunchCompleteCheckBox);
        dinnerCompleteCheckBox = findViewById(R.id.dinnerCompleteCheckBox);

        breakfastNPOCheckBox = findViewById(R.id.breakfastNPOCheckBox);
        lunchNPOCheckBox = findViewById(R.id.lunchNPOCheckBox);
        dinnerNPOCheckBox = findViewById(R.id.dinnerNPOCheckBox);

        // Action Buttons
        editPatientButton = findViewById(R.id.editPatientButton);
        editMealPlanButton = findViewById(R.id.editMealPlanButton);
        transferPatientButton = findViewById(R.id.transferPatientButton);
        dischargePatientButton = findViewById(R.id.dischargePatientButton);
        markAllCompleteButton = findViewById(R.id.markAllCompleteButton);
        markAllNPOButton = findViewById(R.id.markAllNPOButton);
    }

    private void setupListeners() {
        // Edit Patient Button
        if (editPatientButton != null) {
            editPatientButton.setOnClickListener(v -> editPatientDetails());
        }

        // Edit Meal Plan Button
        if (editMealPlanButton != null) {
            editMealPlanButton.setOnClickListener(v -> editMealPlan());
        }

        // Transfer Patient Button
        if (transferPatientButton != null) {
            transferPatientButton.setOnClickListener(v -> transferPatient());
        }

        // Discharge Patient Button
        if (dischargePatientButton != null) {
            dischargePatientButton.setOnClickListener(v -> dischargePatient());
        }

        // Mark All Complete Button
        if (markAllCompleteButton != null) {
            markAllCompleteButton.setOnClickListener(v -> markAllMealsComplete());
        }

        // Mark All NPO Button
        if (markAllNPOButton != null) {
            markAllNPOButton.setOnClickListener(v -> markAllMealsNPO());
        }

        // Meal Complete/NPO checkboxes
        setupMealCheckboxes();
    }

    private void setupMealCheckboxes() {
        // Breakfast checkboxes
        if (breakfastCompleteCheckBox != null && breakfastNPOCheckBox != null) {
            breakfastCompleteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && breakfastNPOCheckBox.isChecked()) {
                    breakfastNPOCheckBox.setChecked(false);
                }
                updatePatientMealStatus();
            });

            breakfastNPOCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && breakfastCompleteCheckBox.isChecked()) {
                    breakfastCompleteCheckBox.setChecked(false);
                }
                updatePatientMealStatus();
            });
        }

        // Lunch checkboxes
        if (lunchCompleteCheckBox != null && lunchNPOCheckBox != null) {
            lunchCompleteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && lunchNPOCheckBox.isChecked()) {
                    lunchNPOCheckBox.setChecked(false);
                }
                updatePatientMealStatus();
            });

            lunchNPOCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && lunchCompleteCheckBox.isChecked()) {
                    lunchCompleteCheckBox.setChecked(false);
                }
                updatePatientMealStatus();
            });
        }

        // Dinner checkboxes
        if (dinnerCompleteCheckBox != null && dinnerNPOCheckBox != null) {
            dinnerCompleteCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && dinnerNPOCheckBox.isChecked()) {
                    dinnerNPOCheckBox.setChecked(false);
                }
                updatePatientMealStatus();
            });

            dinnerNPOCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && dinnerCompleteCheckBox.isChecked()) {
                    dinnerCompleteCheckBox.setChecked(false);
                }
                updatePatientMealStatus();
            });
        }
    }

    private void loadPatientData() {
        try {
            currentPatient = patientDAO.getPatientById(patientId);
            if (currentPatient == null) {
                Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            displayPatientInformation();
            displayMealStatus();

        } catch (Exception e) {
            Log.e(TAG, "Error loading patient data", e);
            Toast.makeText(this, "Error loading patient data: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPatientInformation() {
        // Patient Name
        String fullName = currentPatient.getPatientFirstName() + " " + currentPatient.getPatientLastName();
        patientNameText.setText(fullName);

        // Location
        String location = currentPatient.getWing() + " - Room " + currentPatient.getRoomNumber();
        locationText.setText("📍 " + location);

        // Diet Type
        String dietInfo = currentPatient.getDiet();
        if (currentPatient.isAdaDiet()) {
            dietInfo += " (ADA)";
        }
        dietText.setText("🍽️ " + dietInfo);

        // Fluid Restriction
        String fluidInfo = currentPatient.getFluidRestriction();
        if (fluidInfo == null || fluidInfo.isEmpty()) {
            fluidInfo = "None";
        }
        fluidRestrictionText.setText("💧 Fluid Restriction: " + fluidInfo);

        // Texture Modifications
        List<String> textureMods = new ArrayList<>();
        if (currentPatient.isMechanicalGround()) textureMods.add("Mechanical Ground");
        if (currentPatient.isMechanicalChopped()) textureMods.add("Mechanical Chopped");
        if (currentPatient.isBiteSize()) textureMods.add("Bite Size");
        if (currentPatient.isBreadOK()) textureMods.add("Bread OK");

        String textureInfo = textureMods.isEmpty() ? "None" : String.join(", ", textureMods);
        textureModificationsText.setText("🥄 Texture: " + textureInfo);

        // Liquid Thickness
        List<String> liquidMods = new ArrayList<>();
        if (currentPatient.isNectarThick()) liquidMods.add("Nectar Thick");
        if (currentPatient.isPuddingThick()) liquidMods.add("Pudding Thick");

        String liquidInfo = liquidMods.isEmpty() ? "Regular" : String.join(", ", liquidMods);
        liquidThicknessText.setText("🥤 Liquid Thickness: " + liquidInfo);

        // Created Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String createdDate = dateFormat.format(currentPatient.getCreatedDate());
        createdDateText.setText("📅 Created: " + createdDate);
    }

    private void displayMealStatus() {
        // Breakfast
        String breakfastDiet = currentPatient.getBreakfastDiet() != null ?
                currentPatient.getBreakfastDiet() : currentPatient.getDiet();
        breakfastDietText.setText("Diet: " + breakfastDiet);

        String breakfastMenu = formatMealItems(
                currentPatient.getBreakfastMain(),
                currentPatient.getBreakfastSide(),
                currentPatient.getBreakfastDrink(),
                currentPatient.getBreakfastJuices()
        );
        breakfastMenuText.setText(breakfastMenu);

        breakfastCompleteCheckBox.setChecked(currentPatient.isBreakfastComplete());
        breakfastNPOCheckBox.setChecked(currentPatient.isBreakfastNPO());

        // Lunch
        String lunchDiet = currentPatient.getLunchDiet() != null ?
                currentPatient.getLunchDiet() : currentPatient.getDiet();
        lunchDietText.setText("Diet: " + lunchDiet);

        String lunchMenu = formatMealItems(
                currentPatient.getLunchMain(),
                currentPatient.getLunchSide(),
                currentPatient.getLunchDrink(),
                currentPatient.getLunchJuices()
        );
        lunchMenuText.setText(lunchMenu);

        lunchCompleteCheckBox.setChecked(currentPatient.isLunchComplete());
        lunchNPOCheckBox.setChecked(currentPatient.isLunchNPO());

        // Dinner
        String dinnerDiet = currentPatient.getDinnerDiet() != null ?
                currentPatient.getDinnerDiet() : currentPatient.getDiet();
        dinnerDietText.setText("Diet: " + dinnerDiet);

        String dinnerMenu = formatMealItems(
                currentPatient.getDinnerMain(),
                currentPatient.getDinnerSide(),
                currentPatient.getDinnerDrink(),
                currentPatient.getDinnerJuices()
        );
        dinnerMenuText.setText(dinnerMenu);

        dinnerCompleteCheckBox.setChecked(currentPatient.isDinnerComplete());
        dinnerNPOCheckBox.setChecked(currentPatient.isDinnerNPO());
    }

    private String formatMealItems(String main, String side, String drink, String juice) {
        StringBuilder menu = new StringBuilder();

        if (main != null && !main.isEmpty()) {
            menu.append("Main: ").append(main).append("\n");
        }
        if (side != null && !side.isEmpty()) {
            menu.append("Side: ").append(side).append("\n");
        }
        if (drink != null && !drink.isEmpty()) {
            menu.append("Drink: ").append(drink).append("\n");
        }
        if (juice != null && !juice.isEmpty()) {
            menu.append("Juice: ").append(juice);
        }

        String result = menu.toString().trim();
        return result.isEmpty() ? "No meal items selected" : result;
    }

    private void updatePatientMealStatus() {
        try {
            // Update meal completion status
            currentPatient.setBreakfastComplete(breakfastCompleteCheckBox.isChecked());
            currentPatient.setLunchComplete(lunchCompleteCheckBox.isChecked());
            currentPatient.setDinnerComplete(dinnerCompleteCheckBox.isChecked());

            // Update NPO status
            currentPatient.setBreakfastNPO(breakfastNPOCheckBox.isChecked());
            currentPatient.setLunchNPO(lunchNPOCheckBox.isChecked());
            currentPatient.setDinnerNPO(dinnerNPOCheckBox.isChecked());

            // Save to database
            patientDAO.updatePatient(currentPatient);

        } catch (Exception e) {
            Log.e(TAG, "Error updating meal status", e);
            Toast.makeText(this, "Error updating meal status", Toast.LENGTH_SHORT).show();
        }
    }

    private void editPatientDetails() {
        Intent intent = new Intent(this, NewPatientActivity.class);
        intent.putExtra("edit_patient_id", patientId);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivityForResult(intent, 1);
    }

    private void editMealPlan() {
        Intent intent = new Intent(this, MealPlanningActivity.class);
        intent.putExtra("patient_id", (long) patientId);
        intent.putExtra("diet", currentPatient.getDiet());
        intent.putExtra("is_ada_diet", currentPatient.isAdaDiet());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivityForResult(intent, 2);
    }

    private void transferPatient() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Transfer Patient");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transfer_patient, null);
        builder.setView(dialogView);

        Spinner wingSpinner = dialogView.findViewById(R.id.wingSpinner);
        Spinner roomSpinner = dialogView.findViewById(R.id.roomSpinner);

        // Setup wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, wings);
        wingSpinner.setAdapter(wingAdapter);

        // Set current wing
        int currentWingIndex = -1;
        for (int i = 0; i < wings.length; i++) {
            if (wings[i].equals(currentPatient.getWing())) {
                currentWingIndex = i;
                break;
            }
        }
        if (currentWingIndex >= 0) {
            wingSpinner.setSelection(currentWingIndex);
        }

        // Setup room spinner based on selected wing
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = wings[position];
                String[] rooms = wingRoomMap.get(selectedWing);

                ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(PatientDetailActivity.this,
                        android.R.layout.simple_spinner_dropdown_item, rooms);
                roomSpinner.setAdapter(roomAdapter);

                // Set current room if on same wing
                if (selectedWing.equals(currentPatient.getWing())) {
                    for (int i = 0; i < rooms.length; i++) {
                        if (rooms[i].equals(currentPatient.getRoomNumber())) {
                            roomSpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        builder.setPositiveButton("Transfer", (dialog, which) -> {
            String newWing = wingSpinner.getSelectedItem().toString();
            String newRoom = roomSpinner.getSelectedItem().toString();

            if (newWing.equals(currentPatient.getWing()) &&
                    newRoom.equals(currentPatient.getRoomNumber())) {
                Toast.makeText(this, "Patient is already in this location",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Update patient location
            currentPatient.setWing(newWing);
            currentPatient.setRoomNumber(newRoom);

            try {
                patientDAO.updatePatient(currentPatient);
                Toast.makeText(this, "Patient transferred successfully",
                        Toast.LENGTH_SHORT).show();
                loadPatientData(); // Refresh display
            } catch (Exception e) {
                Log.e(TAG, "Error transferring patient", e);
                Toast.makeText(this, "Error transferring patient",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void dischargePatient() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Discharge Patient");
        builder.setMessage("Are you sure you want to discharge " +
                currentPatient.getPatientFirstName() + " " +
                currentPatient.getPatientLastName() + "?");

        builder.setPositiveButton("Discharge", (dialog, which) -> {
            try {
                currentPatient.setDischarged(true);
                patientDAO.updatePatient(currentPatient);
                Toast.makeText(this, "Patient discharged successfully",
                        Toast.LENGTH_SHORT).show();
                finish(); // Return to previous screen
            } catch (Exception e) {
                Log.e(TAG, "Error discharging patient", e);
                Toast.makeText(this, "Error discharging patient",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void markAllMealsComplete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mark All Meals Complete");
        builder.setMessage("Mark all meals as complete for today?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            breakfastCompleteCheckBox.setChecked(true);
            lunchCompleteCheckBox.setChecked(true);
            dinnerCompleteCheckBox.setChecked(true);

            breakfastNPOCheckBox.setChecked(false);
            lunchNPOCheckBox.setChecked(false);
            dinnerNPOCheckBox.setChecked(false);

            updatePatientMealStatus();
            Toast.makeText(this, "All meals marked as complete", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void markAllMealsNPO() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Mark All Meals NPO");
        builder.setMessage("Mark all meals as NPO (Nothing by mouth) for today?");

        builder.setPositiveButton("Yes", (dialog, which) -> {
            breakfastNPOCheckBox.setChecked(true);
            lunchNPOCheckBox.setChecked(true);
            dinnerNPOCheckBox.setChecked(true);

            breakfastCompleteCheckBox.setChecked(false);
            lunchCompleteCheckBox.setChecked(false);
            dinnerCompleteCheckBox.setChecked(false);

            updatePatientMealStatus();
            Toast.makeText(this, "All meals marked as NPO", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadPatientData(); // Refresh patient data
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_patient_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_refresh) {
            loadPatientData();
            Toast.makeText(this, "Patient data refreshed", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_print) {
            // TODO: Implement print functionality
            Toast.makeText(this, "Print functionality coming soon", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatientData(); // Refresh data when returning to this activity
    }
}