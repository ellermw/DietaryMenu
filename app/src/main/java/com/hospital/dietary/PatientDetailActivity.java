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
    private TextView allergiesText;
    private TextView likesText;
    private TextView dislikesText;
    private TextView commentsText;
    private TextView createdDateText;

    // UI Components - Meal Status
    private LinearLayout mealStatusSection;
    private CheckBox breakfastCompleteCheckBox;
    private CheckBox lunchCompleteCheckBox;
    private CheckBox dinnerCompleteCheckBox;
    private TextView orderDateText;

    // UI Components - Meal Items
    private LinearLayout mealItemsSection;
    private TextView breakfastItemsText;
    private TextView lunchItemsText;
    private TextView dinnerItemsText;
    private TextView breakfastDrinksText;
    private TextView lunchDrinksText;
    private TextView dinnerDrinksText;

    // UI Components - Action Buttons
    private Button editPatientButton;
    private Button editMealPlanButton;
    private Button dischargePatientButton;
    private Button transferRoomButton;

    // Wing-Room mapping
    private Map<String, String[]> wingRoomMap = new HashMap<>();
    private String[] wings = {"1 South", "2 North", "Labor and Delivery", "2 West", "3 North", "ICU"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);

        // Get intent data
        patientId = getIntent().getIntExtra("patient_id", -1);
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        if (patientId == -1) {
            Toast.makeText(this, "Error: Invalid patient ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Setup wing-room mapping
        setupWingRoomMapping();

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Patient Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI
        initializeUI();

        // Load patient data
        loadPatientData();

        // Setup listeners
        setupListeners();
    }

    private void setupWingRoomMapping() {
        wingRoomMap.put("1 South", generateRoomNumbers(100, 126));
        wingRoomMap.put("2 North", generateRoomNumbers(200, 227));
        wingRoomMap.put("Labor and Delivery", generateRoomNumbers(300, 310));
        wingRoomMap.put("2 West", generateRoomNumbers(228, 250));
        wingRoomMap.put("3 North", generateRoomNumbers(301, 325));
        wingRoomMap.put("ICU", generateRoomNumbers(1, 10));
    }

    private String[] generateRoomNumbers(int start, int end) {
        String[] rooms = new String[end - start + 1];
        for (int i = 0; i < rooms.length; i++) {
            rooms[i] = String.valueOf(start + i);
        }
        return rooms;
    }

    private void initializeUI() {
        // Patient Information
        patientNameText = findViewById(R.id.patientNameText);
        locationText = findViewById(R.id.locationText);
        dietText = findViewById(R.id.dietText);
        fluidRestrictionText = findViewById(R.id.fluidRestrictionText);
        textureModificationsText = findViewById(R.id.textureModificationsText);
        allergiesText = findViewById(R.id.allergiesText);
        likesText = findViewById(R.id.likesText);
        dislikesText = findViewById(R.id.dislikesText);
        commentsText = findViewById(R.id.commentsText);
        createdDateText = findViewById(R.id.createdDateText);

        // Meal Status
        mealStatusSection = findViewById(R.id.mealStatusSection);
        breakfastCompleteCheckBox = findViewById(R.id.breakfastCompleteCheckBox);
        lunchCompleteCheckBox = findViewById(R.id.lunchCompleteCheckBox);
        dinnerCompleteCheckBox = findViewById(R.id.dinnerCompleteCheckBox);
        orderDateText = findViewById(R.id.orderDateText);

        // Meal Items
        mealItemsSection = findViewById(R.id.mealItemsSection);
        breakfastItemsText = findViewById(R.id.breakfastItemsText);
        lunchItemsText = findViewById(R.id.lunchItemsText);
        dinnerItemsText = findViewById(R.id.dinnerItemsText);
        breakfastDrinksText = findViewById(R.id.breakfastDrinksText);
        lunchDrinksText = findViewById(R.id.lunchDrinksText);
        dinnerDrinksText = findViewById(R.id.dinnerDrinksText);

        // Action Buttons
        editPatientButton = findViewById(R.id.editPatientButton);
        editMealPlanButton = findViewById(R.id.editMealPlanButton);
        dischargePatientButton = findViewById(R.id.dischargePatientButton);
        transferRoomButton = findViewById(R.id.transferRoomButton);
    }

    private void loadPatientData() {
        try {
            currentPatient = patientDAO.getPatientById(patientId);

            if (currentPatient == null) {
                Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            updateUI();
        } catch (Exception e) {
            Log.e(TAG, "Error loading patient data", e);
            Toast.makeText(this, "Error loading patient data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updateUI() {
        if (currentPatient == null) return;

        // Patient Information
        patientNameText.setText(currentPatient.getFullName());
        locationText.setText(currentPatient.getWing() + " - Room " + currentPatient.getRoomNumber());

        // Diet information
        String dietInfo = currentPatient.getDiet();
        if (currentPatient.isAdaDiet()) {
            dietInfo += " (ADA)";
        }
        dietText.setText(dietInfo);

        fluidRestrictionText.setText(currentPatient.getFluidRestriction());

        // Texture modifications
        StringBuilder textureMods = new StringBuilder();
        if (currentPatient.isMechanicalGround()) textureMods.append("Mechanical Ground, ");
        if (currentPatient.isMechanicalChopped()) textureMods.append("Mechanical Chopped, ");
        if (currentPatient.isBiteSize()) textureMods.append("Bite Size, ");
        if (currentPatient.isBreadOK()) textureMods.append("Bread OK, ");
        if (currentPatient.isExtraGravy()) textureMods.append("Extra Gravy, ");
        if (currentPatient.isMeatsOnly()) textureMods.append("Meats Only, ");

        // Thicken liquids
        if (currentPatient.isNectarThick()) textureMods.append("Nectar Thick, ");
        if (currentPatient.isHoneyThick()) textureMods.append("Honey Thick, ");
        if (currentPatient.isPuddingThick()) textureMods.append("Pudding Thick, ");

        String textureStr = textureMods.length() > 0 ?
                textureMods.substring(0, textureMods.length() - 2) : "None";
        textureModificationsText.setText(textureStr);

        // Allergies, Likes, Dislikes, Comments
        allergiesText.setText(currentPatient.getAllergies() != null && !currentPatient.getAllergies().isEmpty() ?
                currentPatient.getAllergies() : "None");
        likesText.setText(currentPatient.getLikes() != null && !currentPatient.getLikes().isEmpty() ?
                currentPatient.getLikes() : "None");
        dislikesText.setText(currentPatient.getDislikes() != null && !currentPatient.getDislikes().isEmpty() ?
                currentPatient.getDislikes() : "None");
        commentsText.setText(currentPatient.getComments() != null && !currentPatient.getComments().isEmpty() ?
                currentPatient.getComments() : "None");

        // Created date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        createdDateText.setText("Admitted: " + dateFormat.format(new Date(currentPatient.getCreatedAt())));

        // Meal completion status
        breakfastCompleteCheckBox.setChecked(currentPatient.isBreakfastComplete());
        lunchCompleteCheckBox.setChecked(currentPatient.isLunchComplete());
        dinnerCompleteCheckBox.setChecked(currentPatient.isDinnerComplete());

        // Order date
        orderDateText.setText("Today's Orders");

        // Meal items
        updateMealItems();

        // Show/hide meal sections based on whether patient has orders
        boolean hasOrders = currentPatient.getBreakfastMain() != null ||
                currentPatient.getLunchMain() != null ||
                currentPatient.getDinnerMain() != null;
        mealItemsSection.setVisibility(hasOrders ? View.VISIBLE : View.GONE);
        mealStatusSection.setVisibility(hasOrders ? View.VISIBLE : View.GONE);
    }

    private void updateMealItems() {
        // Breakfast
        String breakfastMain = currentPatient.getBreakfastMain();
        String breakfastSide = currentPatient.getBreakfastSide();
        String breakfastDrink = currentPatient.getBreakfastDrink();

        if (breakfastMain != null || breakfastSide != null) {
            StringBuilder breakfast = new StringBuilder();
            if (breakfastMain != null) breakfast.append("• ").append(breakfastMain);
            if (breakfastSide != null) {
                if (breakfast.length() > 0) breakfast.append("\n");
                breakfast.append("• ").append(breakfastSide);
            }
            breakfastItemsText.setText(breakfast.toString());
        } else {
            breakfastItemsText.setText("No items ordered");
        }

        breakfastDrinksText.setText(breakfastDrink != null ? breakfastDrink : "No drink ordered");

        // Lunch
        String lunchMain = currentPatient.getLunchMain();
        String lunchSide = currentPatient.getLunchSide();
        String lunchDrink = currentPatient.getLunchDrink();

        if (lunchMain != null || lunchSide != null) {
            StringBuilder lunch = new StringBuilder();
            if (lunchMain != null) lunch.append("• ").append(lunchMain);
            if (lunchSide != null) {
                if (lunch.length() > 0) lunch.append("\n");
                lunch.append("• ").append(lunchSide);
            }
            lunchItemsText.setText(lunch.toString());
        } else {
            lunchItemsText.setText("No items ordered");
        }

        lunchDrinksText.setText(lunchDrink != null ? lunchDrink : "No drink ordered");

        // Dinner
        String dinnerMain = currentPatient.getDinnerMain();
        String dinnerSide = currentPatient.getDinnerSide();
        String dinnerDrink = currentPatient.getDinnerDrink();

        if (dinnerMain != null || dinnerSide != null) {
            StringBuilder dinner = new StringBuilder();
            if (dinnerMain != null) dinner.append("• ").append(dinnerMain);
            if (dinnerSide != null) {
                if (dinner.length() > 0) dinner.append("\n");
                dinner.append("• ").append(dinnerSide);
            }
            dinnerItemsText.setText(dinner.toString());
        } else {
            dinnerItemsText.setText("No items ordered");
        }

        dinnerDrinksText.setText(dinnerDrink != null ? dinnerDrink : "No drink ordered");
    }

    private void setupListeners() {
        // Edit Patient Button
        if (editPatientButton != null) {
            editPatientButton.setOnClickListener(v -> editPatientInfo());
        }

        // Edit Meal Plan Button
        if (editMealPlanButton != null) {
            editMealPlanButton.setOnClickListener(v -> editMealPlan());
        }

        // Discharge Patient Button
        if (dischargePatientButton != null) {
            dischargePatientButton.setOnClickListener(v -> confirmDischargePatient());
        }

        // Transfer Room Button
        if (transferRoomButton != null) {
            transferRoomButton.setOnClickListener(v -> showTransferRoomDialog());
        }

        // Meal status checkboxes are read-only in this view
        breakfastCompleteCheckBox.setEnabled(false);
        lunchCompleteCheckBox.setEnabled(false);
        dinnerCompleteCheckBox.setEnabled(false);
    }

    private void editPatientInfo() {
        Intent intent = new Intent(this, EditPatientActivity.class);
        intent.putExtra("patient_id", patientId);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void editMealPlan() {
        Intent intent = new Intent(this, MealPlanningActivity.class);
        intent.putExtra("patient_id", (long) patientId);
        intent.putExtra("diet", currentPatient.getDiet());
        intent.putExtra("is_ada", currentPatient.isAdaDiet());
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        startActivity(intent);
    }

    private void confirmDischargePatient() {
        new AlertDialog.Builder(this)
                .setTitle("Discharge Patient")
                .setMessage("Are you sure you want to discharge " + currentPatient.getFullName() + "?\n\n" +
                        "This will move the patient to Retired Orders and remove them from active lists.")
                .setPositiveButton("Discharge", (dialog, which) -> dischargePatient())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void dischargePatient() {
        try {
            currentPatient.setDischarged(true);
            boolean success = patientDAO.updatePatient(currentPatient);

            if (success) {
                Toast.makeText(this, "Patient discharged successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to discharge patient", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error discharging patient", e);
            Toast.makeText(this, "Error discharging patient: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showTransferRoomDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_transfer_room, null);
        Spinner wingSpinner = dialogView.findViewById(R.id.wingSpinner);
        Spinner roomSpinner = dialogView.findViewById(R.id.roomSpinner);

        // Setup wing spinner
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);

        // Set current wing
        for (int i = 0; i < wings.length; i++) {
            if (wings[i].equals(currentPatient.getWing())) {
                wingSpinner.setSelection(i);
                break;
            }
        }

        // Wing selection listener
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = wings[position];
                String[] rooms = wingRoomMap.get(selectedWing);
                if (rooms != null) {
                    ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(PatientDetailActivity.this,
                            android.R.layout.simple_spinner_item, rooms);
                    roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    roomSpinner.setAdapter(roomAdapter);

                    // Set current room if same wing
                    if (selectedWing.equals(currentPatient.getWing())) {
                        for (int i = 0; i < rooms.length; i++) {
                            if (rooms[i].equals(currentPatient.getRoomNumber())) {
                                roomSpinner.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        new AlertDialog.Builder(this)
                .setTitle("Transfer Room")
                .setView(dialogView)
                .setPositiveButton("Transfer", (dialog, which) -> {
                    String newWing = wingSpinner.getSelectedItem().toString();
                    String newRoom = roomSpinner.getSelectedItem().toString();
                    transferRoom(newWing, newRoom);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void transferRoom(String newWing, String newRoom) {
        // Check if room is already occupied
        List<Patient> activePatients = patientDAO.getActivePatients();
        Patient occupyingPatient = null;

        for (Patient patient : activePatients) {
            if (patient.getPatientId() != patientId &&
                    patient.getWing().equals(newWing) &&
                    patient.getRoomNumber().equals(newRoom)) {
                occupyingPatient = patient;
                break;
            }
        }

        if (occupyingPatient != null) {
            // Room is occupied
            final Patient patientToDischarge = occupyingPatient;
            new AlertDialog.Builder(this)
                    .setTitle("Room Already Occupied")
                    .setMessage("Room " + newWing + "-" + newRoom + " is currently assigned to " +
                            patientToDischarge.getFullName() + ".\n\n" +
                            "Would you like to discharge the existing patient and transfer " +
                            currentPatient.getFullName() + " to this room?")
                    .setPositiveButton("Discharge & Transfer", (dialog, which) -> {
                        // Discharge the existing patient
                        patientToDischarge.setDischarged(true);
                        patientDAO.updatePatient(patientToDischarge);
                        // Transfer current patient
                        performRoomTransfer(newWing, newRoom);
                    })
                    .setNegativeButton("Choose Different Room", null)
                    .setNeutralButton("Cancel", null)
                    .show();
        } else {
            // Room is available
            performRoomTransfer(newWing, newRoom);
        }
    }

    private void performRoomTransfer(String newWing, String newRoom) {
        try {
            currentPatient.setWing(newWing);
            currentPatient.setRoomNumber(newRoom);
            boolean success = patientDAO.updatePatient(currentPatient);

            if (success) {
                Toast.makeText(this, "Room transferred successfully", Toast.LENGTH_SHORT).show();
                loadPatientData(); // Reload to show updated info
            } else {
                Toast.makeText(this, "Failed to transfer room", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error transferring room", e);
            Toast.makeText(this, "Error transferring room: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatientData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_patient_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_refresh) {
            loadPatientData();
            return true;
        } else if (itemId == R.id.action_edit) {
            editPatientInfo();
            return true;
        } else if (itemId == R.id.action_delete) {
            confirmDischargePatient();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}