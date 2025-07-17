package com.hospital.dietary;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.models.Patient;
import com.hospital.dietary.models.Item;
import java.util.*;

public class MealPlanningActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private ItemDAO itemDAO;
    
    // User and patient information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    private int patientId;
    private String patientName;
    private String wing;
    private String room;
    private String diet;
    private String fluidRestriction;
    private String textureModifications;
    
    // UI Components
    private TextView patientInfoText;
    private Button saveOrderButton;
    private Button backButton;
    
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
    
    // Meal completion status
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
        itemDAO = new ItemDAO(dbHelper);
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load patient data and setup meals
        loadPatientData();
        
        // Setup meal content based on diet
        setupMealContent();
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
    
    private void addClearLiquidItem(LinearLayout container, String itemName, String size) {
        TextView itemView = new TextView(this);
        String displayText = "• " + itemName;
        if (!size.isEmpty()) {
            displayText += " (" + size + ")";
        }
        itemView.setText(displayText);
        itemView.setTextSize(16);
        itemView.setTextColor(0xFF2c3e50);
        itemView.setPadding(16, 8, 16, 8);
        container.addView(itemView);
    }
    
    private void setupRegularMeals() {
        // For regular diets, setup meal selection interface
        setupRegularMealSection(breakfastItemsContainer, "Breakfast");
        setupRegularMealSection(lunchItemsContainer, "Lunch");
        setupRegularMealSection(dinnerItemsContainer, "Dinner");
        
        // These would need to be manually completed by selecting items
        breakfastComplete = false;
        lunchComplete = false;
        dinnerComplete = false;
    }
    
    private void setupRegularMealSection(LinearLayout container, String mealType) {
        // For now, add a simple completion button for non-Clear Liquid diets
        Button completeButton = new Button(this);
        completeButton.setText("Mark " + mealType + " Complete");
        completeButton.setBackgroundColor(0xFF3498db);
        completeButton.setTextColor(0xFFFFFFFF);
        completeButton.setOnClickListener(v -> {
            switch (mealType) {
                case "Breakfast":
                    breakfastComplete = true;
                    break;
                case "Lunch":
                    lunchComplete = true;
                    break;
                case "Dinner":
                    dinnerComplete = true;
                    break;
            }
            completeButton.setText(mealType + " Complete ✓");
            completeButton.setBackgroundColor(0xFF27ae60);
            completeButton.setEnabled(false);
            updateSaveButtonState();
        });
        
        container.addView(completeButton);
    }
    
    private void updateSaveButtonState() {
        boolean canSave = (breakfastComplete || breakfastNPOCheckbox.isChecked()) &&
                         (lunchComplete || lunchNPOCheckbox.isChecked()) &&
                         (dinnerComplete || dinnerNPOCheckbox.isChecked());
        
        saveOrderButton.setEnabled(canSave);
        saveOrderButton.setBackgroundColor(canSave ? 0xFF27ae60 : 0xFF95a5a6);
    }
    
    private void saveOrder() {
        if (patientId == -1) {
            Toast.makeText(this, "Error: Patient ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Update patient meal completion status
            Patient patient = patientDAO.getPatientById(patientId);
            if (patient != null) {
                patient.setBreakfastComplete(breakfastComplete);
                patient.setLunchComplete(lunchComplete);
                patient.setDinnerComplete(dinnerComplete);
                patient.setBreakfastNPO(breakfastNPOCheckbox.isChecked());
                patient.setLunchNPO(lunchNPOCheckbox.isChecked());
                patient.setDinnerNPO(dinnerNPOCheckbox.isChecked());
                
                boolean success = patientDAO.updatePatient(patient);
                if (success) {
                    Toast.makeText(this, "Order saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save order", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}