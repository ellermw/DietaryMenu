package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;

public class MealPlanningActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    
    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    // Patient information
    private int patientId;
    private String patientName;
    private String wing;
    private String room;
    private String diet;
    private String fluidRestriction;
    private String textureModifications;
    
    // UI Components
    private Toolbar toolbar;
    private TextView patientInfoText;
    private Button saveOrderButton;
    private Button backButton;
    private Button homeButton;
    
    // Meal sections
    private LinearLayout breakfastSection;
    private LinearLayout lunchSection;
    private LinearLayout dinnerSection;
    
    // NPO checkboxes
    private CheckBox breakfastNPOCheckbox;
    private CheckBox lunchNPOCheckbox;
    private CheckBox dinnerNPOCheckbox;
    
    // Meal content containers
    private LinearLayout breakfastItemsContainer;
    private LinearLayout lunchItemsContainer;
    private LinearLayout dinnerItemsContainer;
    
    // Completion tracking
    private boolean breakfastComplete = false;
    private boolean lunchComplete = false;
    private boolean dinnerComplete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planning);
        
        // Get information from intent
        extractIntentData();
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        
        // Setup toolbar
        setupToolbar();
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load patient data
        loadPatientData();
        
        // Setup meal content based on diet type
        setupMealContent();
    }

    private void setupToolbar() {
        // toolbar = findViewById(R.id.toolbar);  // Comment this out
        // if (toolbar != null) {
        //     setSupportActionBar(toolbar);
        //     if (getSupportActionBar() != null) {
        //         getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //         getSupportActionBar().setTitle("Finished Orders");
        //     }
        // }

        // Use default action bar instead:
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Finished Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_home, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_home:
                goToMainMenu();
                return true;
            case R.id.action_refresh:
                loadPatientData();
                setupMealContent();
                Toast.makeText(this, "Meal plan refreshed", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void goToMainMenu() {
        Intent intent = new Intent(this, MainMenuActivity.class);
        intent.putExtra("current_user", currentUsername);
        intent.putExtra("user_role", currentUserRole);
        intent.putExtra("user_full_name", currentUserFullName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
    
    private void extractIntentData() {
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        patientId = getIntent().getIntExtra("patient_id", -1);
        patientName = getIntent().getStringExtra("patient_name");
        wing = getIntent().getStringExtra("wing");
        room = getIntent().getStringExtra("room");
        diet = getIntent().getStringExtra("diet");
        fluidRestriction = getIntent().getStringExtra("fluid_restriction");
        textureModifications = getIntent().getStringExtra("texture_modifications");
    }
    
    private void initializeUI() {
        patientInfoText = findViewById(R.id.patientInfoText);
        saveOrderButton = findViewById(R.id.saveOrderButton);
        backButton = findViewById(R.id.backButton);
    //  homeButton = findViewById(R.id.homeButton);
        
        // Meal sections
        breakfastSection = findViewById(R.id.breakfastSection);
        lunchSection = findViewById(R.id.lunchSection);
        dinnerSection = findViewById(R.id.dinnerSection);
        
        // NPO checkboxes
        breakfastNPOCheckbox = findViewById(R.id.breakfastNPOCheckbox);
        lunchNPOCheckbox = findViewById(R.id.lunchNPOCheckbox);
        dinnerNPOCheckbox = findViewById(R.id.dinnerNPOCheckbox);
        
        // Meal content containers
        breakfastItemsContainer = findViewById(R.id.breakfastItemsContainer);
        lunchItemsContainer = findViewById(R.id.lunchItemsContainer);
        dinnerItemsContainer = findViewById(R.id.dinnerItemsContainer);
        
        // Set title
        setTitle("Meal Planning");
        
        // Display patient info
        displayPatientInfo();
    }
    
    private void setupListeners() {
        saveOrderButton.setOnClickListener(v -> saveOrder());
        backButton.setOnClickListener(v -> finish());
        
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> goToMainMenu());
        }
        
        // NPO checkbox listeners
        breakfastNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                breakfastItemsContainer.setVisibility(View.GONE);
                breakfastComplete = true;
            } else {
                breakfastItemsContainer.setVisibility(View.VISIBLE);
                breakfastComplete = false;
            }
            updateSaveButtonState();
        });
        
        lunchNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                lunchItemsContainer.setVisibility(View.GONE);
                lunchComplete = true;
            } else {
                lunchItemsContainer.setVisibility(View.VISIBLE);
                lunchComplete = false;
            }
            updateSaveButtonState();
        });
        
        dinnerNPOCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                dinnerItemsContainer.setVisibility(View.GONE);
                dinnerComplete = true;
            } else {
                dinnerItemsContainer.setVisibility(View.VISIBLE);
                dinnerComplete = false;
            }
            updateSaveButtonState();
        });
    }
    
    private void loadPatientData() {
        if (patientId != -1) {
            Patient patient = patientDAO.getPatientById(patientId);
            if (patient != null) {
                // Update completion status
                breakfastComplete = patient.isBreakfastComplete();
                lunchComplete = patient.isLunchComplete();
                dinnerComplete = patient.isDinnerComplete();
                
                // Update NPO status
                breakfastNPOCheckbox.setChecked(patient.isBreakfastNPO());
                lunchNPOCheckbox.setChecked(patient.isLunchNPO());
                dinnerNPOCheckbox.setChecked(patient.isDinnerNPO());
            }
        }
    }
    
    private void displayPatientInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Patient: ").append(patientName).append("\n");
        info.append("Location: ").append(wing).append(" - Room ").append(room).append("\n");
        info.append("Diet: ").append(diet).append("\n");
        
        if (fluidRestriction != null && !fluidRestriction.equals("None")) {
            info.append("Fluid Restriction: ").append(fluidRestriction).append("\n");
        }
        
        if (textureModifications != null && !textureModifications.isEmpty()) {
            info.append("Texture Modifications: ").append(textureModifications).append("\n");
        }
        
        patientInfoText.setText(info.toString());
    }
    
    private void setupMealContent() {
        if (diet != null && diet.contains("Clear Liquid")) {
            setupClearLiquidMeals();
        } else {
            setupRegularMeals();
        }
        
        updateSaveButtonState();
    }
    
    private void setupClearLiquidMeals() {
        boolean isADA = diet.contains("ADA");
        
        // Clear Liquid menus are fixed and cannot be adjusted
        setupClearLiquidBreakfast(isADA);
        setupClearLiquidLunch(isADA);
        setupClearLiquidDinner(isADA);
        
        // Mark as complete since Clear Liquid is predefined
        if (!breakfastNPOCheckbox.isChecked()) breakfastComplete = true;
        if (!lunchNPOCheckbox.isChecked()) lunchComplete = true;
        if (!dinnerNPOCheckbox.isChecked()) dinnerComplete = true;
    }
    
    private void setupClearLiquidBreakfast(boolean isADA) {
        breakfastItemsContainer.removeAllViews();
        
        // Coffee or Decaf Coffee (200ml)
        addClearLiquidItem(breakfastItemsContainer, "Coffee", "200ml");
        
        // Chicken Broth (200ml)
        addClearLiquidItem(breakfastItemsContainer, "Chicken Broth", "200ml");
        
        // Orange Juice (120ml) or Apple Juice (120ml) if ADA
        String juiceChoice = isADA ? "Apple Juice" : "Orange Juice";
        addClearLiquidItem(breakfastItemsContainer, juiceChoice, "120ml");
        
        // Jello or Sugar Free Jello if ADA
        String jelloChoice = isADA ? "Sugar Free Jello" : "Jello";
        addClearLiquidItem(breakfastItemsContainer, jelloChoice, "");
        
        // Sprite or Sprite Zero if ADA
        String spriteChoice = isADA ? "Sprite Zero" : "Sprite";
        addClearLiquidItem(breakfastItemsContainer, spriteChoice, "240ml");
    }
    
    private void setupClearLiquidLunch(boolean isADA) {
        lunchItemsContainer.removeAllViews();
        
        // Cranberry Juice (120ml)
        addClearLiquidItem(lunchItemsContainer, "Cranberry Juice", "120ml");
        
        // Beef Broth (200ml)
        addClearLiquidItem(lunchItemsContainer, "Beef Broth", "200ml");
        
        // Jello or Sugar Free Jello if ADA
        String jelloChoice = isADA ? "Sugar Free Jello" : "Jello";
        addClearLiquidItem(lunchItemsContainer, jelloChoice, "");
        
        // Ice Tea (240ml)
        addClearLiquidItem(lunchItemsContainer, "Ice Tea", "240ml");
        
        // Sprite or Sprite Zero if ADA
        String spriteChoice = isADA ? "Sprite Zero" : "Sprite";
        addClearLiquidItem(lunchItemsContainer, spriteChoice, "240ml");
    }
    
    private void setupClearLiquidDinner(boolean isADA) {
        dinnerItemsContainer.removeAllViews();
        
        // Apple Juice (120ml)
        addClearLiquidItem(dinnerItemsContainer, "Apple Juice", "120ml");
        
        // Chicken Broth (200ml)
        addClearLiquidItem(dinnerItemsContainer, "Chicken Broth", "200ml");
        
        // Jello or Sugar Free Jello if ADA
        String jelloChoice = isADA ? "Sugar Free Jello" : "Jello";
        addClearLiquidItem(dinnerItemsContainer, jelloChoice, "");
        
        // Ice Tea (200ml)
        addClearLiquidItem(dinnerItemsContainer, "Ice Tea", "200ml");
        
        // Sprite or Sprite Zero if ADA
        String spriteChoice = isADA ? "Sprite Zero" : "Sprite";
        addClearLiquidItem(dinnerItemsContainer, spriteChoice, "240ml");
    }
    
    private void addClearLiquidItem(LinearLayout container, String itemName, String volume) {
        TextView itemView = new TextView(this);
        String displayText = volume.isEmpty() ? itemName : itemName + " (" + volume + ")";
        itemView.setText("• " + displayText);
        itemView.setTextSize(16);
        itemView.setPadding(0, 8, 0, 8);
        container.addView(itemView);
    }
    
    private void setupRegularMeals() {
        // Implementation for regular meal setup
        // This would include meal selection interfaces for non-clear liquid diets
        breakfastComplete = false;
        lunchComplete = false;
        dinnerComplete = false;
    }
    
    private void updateSaveButtonState() {
        boolean allComplete = (breakfastComplete || breakfastNPOCheckbox.isChecked()) &&
                             (lunchComplete || lunchNPOCheckbox.isChecked()) &&
                             (dinnerComplete || dinnerNPOCheckbox.isChecked());
        saveOrderButton.setEnabled(allComplete);
        saveOrderButton.setText(allComplete ? "Save Complete Order" : "Complete All Meals First");
    }
    
    private void saveOrder() {
        if (patientId != -1) {
            Patient patient = patientDAO.getPatientById(patientId);
            if (patient != null) {
                // Update completion status
                patient.setBreakfastComplete(breakfastComplete || breakfastNPOCheckbox.isChecked());
                patient.setLunchComplete(lunchComplete || lunchNPOCheckbox.isChecked());
                patient.setDinnerComplete(dinnerComplete || dinnerNPOCheckbox.isChecked());
                
                // Update NPO status
                patient.setBreakfastNPO(breakfastNPOCheckbox.isChecked());
                patient.setLunchNPO(lunchNPOCheckbox.isChecked());
                patient.setDinnerNPO(dinnerNPOCheckbox.isChecked());
                
                boolean success = patientDAO.updatePatient(patient);
                
                if (success) {
                    Toast.makeText(this, "Order saved successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Return to previous activity
                } else {
                    Toast.makeText(this, "Failed to save order", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}