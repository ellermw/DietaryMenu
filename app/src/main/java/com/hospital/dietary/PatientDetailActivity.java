package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.dao.FinalizedOrderDAO;
import com.hospital.dietary.models.Patient;
import com.hospital.dietary.models.FinalizedOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientDetailActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private FinalizedOrderDAO finalizedOrderDAO;
    
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
    private TextView patientNameText;
    private TextView locationText;
    private TextView dietText;
    private TextView fluidText;
    private TextView textureText;
    private LinearLayout textureContainer;
    private LinearLayout savedMenusContainer;
    private TextView noMenusText;
    private Button editPatientButton;
    private Button planMealsButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_detail);
        
        // Get information from intent
        extractIntentData();
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);
        finalizedOrderDAO = new FinalizedOrderDAO(dbHelper);
        
        // Initialize UI
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load patient data and saved menus
        loadPatientData();
        loadSavedMenus();
    }
    
    private void extractIntentData() {
        patientId = getIntent().getIntExtra("patient_id", 0);
        patientName = getIntent().getStringExtra("patient_name");
        wing = getIntent().getStringExtra("wing");
        room = getIntent().getStringExtra("room");
        diet = getIntent().getStringExtra("diet");
        fluidRestriction = getIntent().getStringExtra("fluid_restriction");
        textureModifications = getIntent().getStringExtra("texture_modifications");
        
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
    }
    
    private void initializeUI() {
        patientNameText = findViewById(R.id.patientNameText);
        locationText = findViewById(R.id.locationText);
        dietText = findViewById(R.id.dietText);
        fluidText = findViewById(R.id.fluidText);
        textureText = findViewById(R.id.textureText);
        textureContainer = findViewById(R.id.textureContainer);
        savedMenusContainer = findViewById(R.id.savedMenusContainer);
        noMenusText = findViewById(R.id.noMenusText);
        editPatientButton = findViewById(R.id.editPatientButton);
        planMealsButton = findViewById(R.id.planMealsButton);
        backButton = findViewById(R.id.backButton);
        
        // Set title
        setTitle("Patient Details");
        
        // Display patient information
        displayPatientInfo();
    }
    
    private void setupListeners() {
        editPatientButton.setOnClickListener(v -> {
            // TODO: Add edit patient functionality
            Toast.makeText(this, "Edit patient functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        planMealsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MealPlanningActivity.class);
            intent.putExtra("patient_id", patientId);
            intent.putExtra("patient_name", patientName);
            intent.putExtra("wing", wing);
            intent.putExtra("room", room);
            intent.putExtra("diet", diet);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            startActivity(intent);
        });
        
        backButton.setOnClickListener(v -> finish());
    }
    
    private void displayPatientInfo() {
        patientNameText.setText(patientName);
        locationText.setText(wing + " - Room " + room);
        dietText.setText(diet);
        fluidText.setText(fluidRestriction != null ? fluidRestriction : "None");
        
        if (textureModifications != null && !textureModifications.isEmpty()) {
            textureText.setText(textureModifications);
            textureContainer.setVisibility(View.VISIBLE);
        } else {
            textureContainer.setVisibility(View.GONE);
        }
    }
    
    private void loadPatientData() {
        // This could be used to refresh patient data if needed
        // For now, we're using the data passed from the intent
    }
    
    private void loadSavedMenus() {
        try {
            // Get today's date
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            
            // Check if there are any saved menus for this room on today's date
            List<FinalizedOrder> savedMenus = finalizedOrderDAO.getOrdersByLocation(wing, room, todayDate);
            
            if (savedMenus.isEmpty()) {
                savedMenusContainer.setVisibility(View.GONE);
                noMenusText.setVisibility(View.VISIBLE);
            } else {
                savedMenusContainer.setVisibility(View.VISIBLE);
                noMenusText.setVisibility(View.GONE);
                
                // Display saved menus
                displaySavedMenus(savedMenus);
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "Error loading saved menus: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void displaySavedMenus(List<FinalizedOrder> savedMenus) {
        savedMenusContainer.removeAllViews();
        
        for (FinalizedOrder order : savedMenus) {
            View menuView = getLayoutInflater().inflate(R.layout.item_saved_menu, null);
            
            TextView dateText = menuView.findViewById(R.id.menuDateText);
            TextView breakfastText = menuView.findViewById(R.id.breakfastItemsText);
            TextView lunchText = menuView.findViewById(R.id.lunchItemsText);
            TextView dinnerText = menuView.findViewById(R.id.dinnerItemsText);
            
            dateText.setText("Menu for " + order.getOrderDate());
            
            // Display meal items
            if (order.getBreakfastItems() != null && !order.getBreakfastItems().isEmpty()) {
                breakfastText.setText("Breakfast: " + String.join(", ", order.getBreakfastItems()));
                breakfastText.setVisibility(View.VISIBLE);
            } else {
                breakfastText.setVisibility(View.GONE);
            }
            
            if (order.getLunchItems() != null && !order.getLunchItems().isEmpty()) {
                lunchText.setText("Lunch: " + String.join(", ", order.getLunchItems()));
                lunchText.setVisibility(View.VISIBLE);
            } else {
                lunchText.setVisibility(View.GONE);
            }
            
            if (order.getDinnerItems() != null && !order.getDinnerItems().isEmpty()) {
                dinnerText.setText("Dinner: " + String.join(", ", order.getDinnerItems()));
                dinnerText.setVisibility(View.VISIBLE);
            } else {
                dinnerText.setVisibility(View.GONE);
            }
            
            savedMenusContainer.addView(menuView);
        }
    }
}