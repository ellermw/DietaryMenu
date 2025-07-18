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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
    private LinearLayout existingOrdersContainer;
    private LinearLayout retiredOrdersContainer;
    private TextView noExistingOrdersText;
    private TextView noRetiredOrdersText;
    private TextView existingOrdersCountText;
    private TextView retiredOrdersCountText;
    private Button editPatientButton;
    private Button planMealsButton;
    private Button backButton;
    private Button refreshOrdersButton;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

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
        
        // Load patient data and all orders
        loadPatientData();
        loadAllOrders();
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
        existingOrdersContainer = findViewById(R.id.existingOrdersContainer);
        retiredOrdersContainer = findViewById(R.id.retiredOrdersContainer);
        noExistingOrdersText = findViewById(R.id.noExistingOrdersText);
        noRetiredOrdersText = findViewById(R.id.noRetiredOrdersText);
        existingOrdersCountText = findViewById(R.id.existingOrdersCountText);
        retiredOrdersCountText = findViewById(R.id.retiredOrdersCountText);
        editPatientButton = findViewById(R.id.editPatientButton);
        planMealsButton = findViewById(R.id.planMealsButton);
        backButton = findViewById(R.id.backButton);
        refreshOrdersButton = findViewById(R.id.refreshOrdersButton);
        
        setTitle("Patient Details");
        
        // Display patient information
        displayPatientInfo();
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        refreshOrdersButton.setOnClickListener(v -> {
            loadAllOrders();
            Toast.makeText(this, "Orders refreshed", Toast.LENGTH_SHORT).show();
        });
        
        editPatientButton.setOnClickListener(v -> {
            // TODO: Implement edit patient functionality
            Toast.makeText(this, "Edit patient functionality coming soon", Toast.LENGTH_SHORT).show();
        });
        
        planMealsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MealPlanningActivity.class);
            intent.putExtra("current_user", currentUsername);
            intent.putExtra("user_role", currentUserRole);
            intent.putExtra("user_full_name", currentUserFullName);
            intent.putExtra("patient_id", patientId);
            intent.putExtra("patient_name", patientName);
            intent.putExtra("wing", wing);
            intent.putExtra("room", room);
            intent.putExtra("diet", diet);
            intent.putExtra("fluid_restriction", fluidRestriction);
            intent.putExtra("texture_modifications", textureModifications);
            startActivity(intent);
        });
    }
    
    private void displayPatientInfo() {
        patientNameText.setText(patientName);
        locationText.setText("📍 " + wing + " - Room " + room);
        dietText.setText("🍽️ " + diet);
        fluidText.setText("💧 " + (fluidRestriction != null && !fluidRestriction.isEmpty() ? 
                          fluidRestriction : "None"));
        
        if (textureModifications != null && !textureModifications.isEmpty()) {
            textureText.setText("⚙️ " + textureModifications);
            textureContainer.setVisibility(View.VISIBLE);
        } else {
            textureContainer.setVisibility(View.GONE);
        }
    }
    
    private void loadPatientData() {
        // This could be used to refresh patient data if needed
        // For now, we're using the data passed from the intent
    }
    
    private void loadAllOrders() {
        try {
            // Calculate cutoff date for retired orders (6 days ago)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -6);
            String cutoffDate = dateFormat.format(calendar.getTime());
            
            // Get all orders for this patient location
            List<FinalizedOrder> allOrders = finalizedOrderDAO.getOrdersByLocation(wing, room);
            
            // Separate existing orders (6 days or newer) from retired orders (older than 6 days)
            List<FinalizedOrder> existingOrders = new java.util.ArrayList<>();
            List<FinalizedOrder> retiredOrders = new java.util.ArrayList<>();
            
            for (FinalizedOrder order : allOrders) {
                try {
                    Date orderDate = dateFormat.parse(order.getOrderDate());
                    Date cutoffDateTime = dateFormat.parse(cutoffDate);
                    
                    if (orderDate.compareTo(cutoffDateTime) >= 0) {
                        // Order is 6 days old or newer - existing order
                        existingOrders.add(order);
                    } else {
                        // Order is older than 6 days - retired order
                        retiredOrders.add(order);
                    }
                } catch (ParseException e) {
                    // If date parsing fails, treat as existing order
                    existingOrders.add(order);
                }
            }
            
            // Display existing orders
            displayExistingOrders(existingOrders);
            
            // Display retired orders
            displayRetiredOrders(retiredOrders);
            
        } catch (Exception e) {
            Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void displayExistingOrders(List<FinalizedOrder> orders) {
        existingOrdersContainer.removeAllViews();
        
        if (orders.isEmpty()) {
            noExistingOrdersText.setVisibility(View.VISIBLE);
            existingOrdersCountText.setVisibility(View.GONE);
        } else {
            noExistingOrdersText.setVisibility(View.GONE);
            existingOrdersCountText.setVisibility(View.VISIBLE);
            existingOrdersCountText.setText(orders.size() + " existing order(s)");
            
            for (FinalizedOrder order : orders) {
                View orderView = createOrderView(order, false);
                existingOrdersContainer.addView(orderView);
            }
        }
    }
    
    private void displayRetiredOrders(List<FinalizedOrder> orders) {
        retiredOrdersContainer.removeAllViews();
        
        if (orders.isEmpty()) {
            noRetiredOrdersText.setVisibility(View.VISIBLE);
            retiredOrdersCountText.setVisibility(View.GONE);
        } else {
            noRetiredOrdersText.setVisibility(View.GONE);
            retiredOrdersCountText.setVisibility(View.VISIBLE);
            retiredOrdersCountText.setText(orders.size() + " retired order(s)");
            
            for (FinalizedOrder order : orders) {
                View orderView = createOrderView(order, true);
                retiredOrdersContainer.addView(orderView);
            }
        }
    }
    
    private View createOrderView(FinalizedOrder order, boolean isRetired) {
        View orderView = getLayoutInflater().inflate(R.layout.item_patient_order, null);
        
        TextView orderDateText = orderView.findViewById(R.id.orderDateText);
        TextView orderStatusText = orderView.findViewById(R.id.orderStatusText);
        TextView breakfastText = orderView.findViewById(R.id.breakfastItemsText);
        TextView lunchText = orderView.findViewById(R.id.lunchItemsText);
        TextView dinnerText = orderView.findViewById(R.id.dinnerItemsText);
        TextView dietTypeText = orderView.findViewById(R.id.dietTypeText);
        
        // Set order date
        try {
            Date orderDate = dateFormat.parse(order.getOrderDate());
            orderDateText.setText("📅 " + displayDateFormat.format(orderDate));
        } catch (ParseException e) {
            orderDateText.setText("📅 " + order.getOrderDate());
        }
        
        // Set order status
        if (isRetired) {
            orderStatusText.setText("🗃️ RETIRED");
            orderStatusText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            orderView.setAlpha(0.7f); // Make retired orders slightly faded
        } else {
            orderStatusText.setText("📋 ACTIVE");
            orderStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        
        // Set diet type
        dietTypeText.setText("🍽️ " + order.getDietType());
        
        // Display meal items
        if (order.getBreakfastItems() != null && !order.getBreakfastItems().isEmpty()) {
            breakfastText.setText("🌅 Breakfast: " + String.join(", ", order.getBreakfastItems()));
            breakfastText.setVisibility(View.VISIBLE);
        } else {
            breakfastText.setVisibility(View.GONE);
        }
        
        if (order.getLunchItems() != null && !order.getLunchItems().isEmpty()) {
            lunchText.setText("☀️ Lunch: " + String.join(", ", order.getLunchItems()));
            lunchText.setVisibility(View.VISIBLE);
        } else {
            lunchText.setVisibility(View.GONE);
        }
        
        if (order.getDinnerItems() != null && !order.getDinnerItems().isEmpty()) {
            dinnerText.setText("🌙 Dinner: " + String.join(", ", order.getDinnerItems()));
            dinnerText.setVisibility(View.VISIBLE);
        } else {
            dinnerText.setVisibility(View.GONE);
        }
        
        // Add click listener for detailed view
        orderView.setOnClickListener(v -> showOrderDetails(order));
        
        // Add some margin between orders
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        orderView.setLayoutParams(params);
        
        return orderView;
    }
    
    private void showOrderDetails(FinalizedOrder order) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Order Details");
        
        StringBuilder details = new StringBuilder();
        details.append("Patient: ").append(order.getPatientName()).append("\n");
        details.append("Date: ").append(order.getOrderDate()).append("\n");
        details.append("Diet: ").append(order.getDietType()).append("\n");
        
        if (order.getFluidRestriction() != null && !order.getFluidRestriction().isEmpty()) {
            details.append("Fluid Restriction: ").append(order.getFluidRestriction()).append("\n");
        }
        
        // Texture modifications
        if (order.isMechanicalGround() || order.isMechanicalChopped() || 
            order.isBiteSize() || order.isBreadOK()) {
            details.append("Texture Modifications: ");
            List<String> modifications = new java.util.ArrayList<>();
            if (order.isMechanicalGround()) modifications.add("Mechanical Ground");
            if (order.isMechanicalChopped()) modifications.add("Mechanical Chopped");
            if (order.isBiteSize()) modifications.add("Bite Size");
            if (order.isBreadOK()) modifications.add("Bread OK");
            details.append(String.join(", ", modifications)).append("\n");
        }
        
        details.append("\n--- Meals ---\n");
        
        if (order.getBreakfastItems() != null && !order.getBreakfastItems().isEmpty()) {
            details.append("Breakfast: ").append(String.join(", ", order.getBreakfastItems())).append("\n");
        }
        
        if (order.getLunchItems() != null && !order.getLunchItems().isEmpty()) {
            details.append("Lunch: ").append(String.join(", ", order.getLunchItems())).append("\n");
        }
        
        if (order.getDinnerItems() != null && !order.getDinnerItems().isEmpty()) {
            details.append("Dinner: ").append(String.join(", ", order.getDinnerItems())).append("\n");
        }
        
        builder.setMessage(details.toString());
        builder.setPositiveButton("Close", null);
        builder.show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh orders when returning to this activity
        loadAllOrders();
    }
}