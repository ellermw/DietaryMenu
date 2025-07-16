// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/MainActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.ItemDAO;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.Item;
import com.hospital.dietary.models.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    
    // UI Components
    private RadioGroup dayGroup;
    private EditText patientNameInput;
    private Spinner wingSpinner, roomSpinner, dietSpinner, fluidRestrictionSpinner;
    
    // Texture modification checkboxes
    private CheckBox mechanicalGroundCB, mechanicalChoppedCB, biteSizeCB, breadOKCB;
    
    // Breakfast components
    private Spinner breakfastColdCereal, breakfastHotCereal, breakfastBread;
    private Spinner breakfastMuffin, breakfastMain, breakfastFruit;
    private LinearLayout breakfastJuicesContainer, breakfastDrinksContainer;
    
    // Lunch components
    private Spinner lunchProtein, lunchStarch, lunchVegetable, lunchDessert;
    private LinearLayout lunchDrinksContainer;
    
    // Dinner components
    private Spinner dinnerProtein, dinnerStarch, dinnerVegetable, dinnerDessert;
    private LinearLayout dinnerDrinksContainer;
    
    // Fluid tracking
    private TextView breakfastFluidTracker, lunchFluidTracker, dinnerFluidTracker;
    private Map<String, Integer> fluidUsed = new HashMap<>();
    private Map<String, Integer> fluidLimits = new HashMap<>();
    
    // Database components
    private DatabaseHelper dbHelper;
    private ItemDAO itemDAO;
    private UserDAO userDAO;
    
    // Current logged in user
    private User currentUser;
    
    // Data lists with updated room numbers
    private List<String> wings = Arrays.asList("1 South", "2 North", "Labor and Delivery", 
                                              "2 West", "3 North", "ICU");
    private List<String> diets = Arrays.asList("Regular", "ADA", "Cardiac", "Renal", 
                                              "Puree", "Full Liquid", "Clear Liquid");
    
    // Updated room numbers per wing as specified
    private Map<String, List<String>> wingRooms = new HashMap<String, List<String>>() {{
        put("1 South", generateRoomNumbers(106, 122));
        put("2 North", generateRoomNumbers(250, 264));
        put("Labor and Delivery", generateLDRRooms());
        put("2 West", generateRoomNumbers(225, 248));
        put("3 North", generateRoomNumbers(349, 371));
        put("ICU", generateICURooms());
    }};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        itemDAO = new ItemDAO(dbHelper);
        userDAO = new UserDAO(dbHelper);
        
        // Show login dialog
        showLoginDialog();
    }
    
    private void showLoginDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_login, null);
        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Login")
            .setMessage("Please login to continue:")
            .setView(dialogView)
            .setPositiveButton("Login", null)
            .setCancelable(false)
            .create();
        
        dialog.setOnShowListener(dialogInterface -> {
            Button loginButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            loginButton.setOnClickListener(v -> {
                String username = usernameInput.getText().toString().trim();
                String password = passwordInput.getText().toString();
                
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                User user = userDAO.authenticateUser(username, password);
                if (user != null) {
                    currentUser = user;
                    dialog.dismiss();
                    initializeAfterLogin();
                    Toast.makeText(this, "Welcome, " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();
                } else {
                    passwordInput.setError("Invalid username or password");
                    passwordInput.selectAll();
                }
            });
        });
        
        dialog.show();
        usernameInput.requestFocus();
    }
    
    private void initializeAfterLogin() {
        // Initialize UI components
        initializeUI();
        
        // Setup listeners
        setupListeners();
        
        // Load initial data
        loadSpinnerData();
        
        // Setup fluid tracking
        setupFluidTracking();
    }
    
    private void initializeUI() {
        // Find UI components
        dayGroup = findViewById(R.id.dayGroup);
        patientNameInput = findViewById(R.id.patientNameInput);
        wingSpinner = findViewById(R.id.wingSpinner);
        roomSpinner = findViewById(R.id.roomSpinner);
        dietSpinner = findViewById(R.id.dietSpinner);
        fluidRestrictionSpinner = findViewById(R.id.fluidRestrictionSpinner);
        
        // Texture modification checkboxes
        mechanicalGroundCB = findViewById(R.id.mechanicalGroundCB);
        mechanicalChoppedCB = findViewById(R.id.mechanicalChoppedCB);
        biteSizeCB = findViewById(R.id.biteSizeCB);
        breadOKCB = findViewById(R.id.breadOKCB);
        
        // Breakfast components
        breakfastColdCereal = findViewById(R.id.breakfastColdCereal);
        breakfastHotCereal = findViewById(R.id.breakfastHotCereal);
        breakfastBread = findViewById(R.id.breakfastBread);
        breakfastMuffin = findViewById(R.id.breakfastMuffin);
        breakfastMain = findViewById(R.id.breakfastMain);
        breakfastFruit = findViewById(R.id.breakfastFruit);
        breakfastJuicesContainer = findViewById(R.id.breakfastJuicesContainer);
        breakfastDrinksContainer = findViewById(R.id.breakfastDrinksContainer);
        
        // Lunch components
        lunchProtein = findViewById(R.id.lunchProtein);
        lunchStarch = findViewById(R.id.lunchStarch);
        lunchVegetable = findViewById(R.id.lunchVegetable);
        lunchDessert = findViewById(R.id.lunchDessert);
        lunchDrinksContainer = findViewById(R.id.lunchDrinksContainer);
        
        // Dinner components
        dinnerProtein = findViewById(R.id.dinnerProtein);
        dinnerStarch = findViewById(R.id.dinnerStarch);
        dinnerVegetable = findViewById(R.id.dinnerVegetable);
        dinnerDessert = findViewById(R.id.dinnerDessert);
        dinnerDrinksContainer = findViewById(R.id.dinnerDrinksContainer);
        
        // Fluid trackers
        breakfastFluidTracker = findViewById(R.id.breakfastFluidTracker);
        lunchFluidTracker = findViewById(R.id.lunchFluidTracker);
        dinnerFluidTracker = findViewById(R.id.dinnerFluidTracker);
    }
    
    private void setupListeners() {
        // Wing selection listener
        wingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWing = wings.get(position);
                loadRoomsForWing(selectedWing);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Add other listeners as needed...
    }
    
    private void loadSpinnerData() {
        // Load wings
        ArrayAdapter<String> wingAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, wings);
        wingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wingSpinner.setAdapter(wingAdapter);
        
        // Load diets
        ArrayAdapter<String> dietAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, diets);
        dietAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietSpinner.setAdapter(dietAdapter);
        
        // Load initial rooms (for first wing)
        if (!wings.isEmpty()) {
            loadRoomsForWing(wings.get(0));
        }
    }
    
    private void loadRoomsForWing(String wing) {
        List<String> rooms = wingRooms.get(wing);
        if (rooms != null) {
            ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(this, 
                    android.R.layout.simple_spinner_item, rooms);
            roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            roomSpinner.setAdapter(roomAdapter);
        }
    }
    
    private List<String> generateRoomNumbers(int start, int end) {
        List<String> rooms = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            rooms.add(String.valueOf(i));
        }
        return rooms;
    }
    
    private List<String> generateLDRRooms() {
        List<String> rooms = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            rooms.add("LDR" + i);
        }
        return rooms;
    }
    
    private List<String> generateICURooms() {
        List<String> rooms = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            rooms.add("ICU" + i);
        }
        return rooms;
    }
    
    private void setupFluidTracking() {
        // Initialize fluid tracking
        fluidUsed.put("Breakfast", 0);
        fluidUsed.put("Lunch", 0);
        fluidUsed.put("Dinner", 0);
        
        updateFluidDisplay();
    }
    
    private void updateFluidDisplay() {
        breakfastFluidTracker.setText("Fluids: " + fluidUsed.get("Breakfast") + "ml");
        lunchFluidTracker.setText("Fluids: " + fluidUsed.get("Lunch") + "ml");
        dinnerFluidTracker.setText("Fluids: " + fluidUsed.get("Dinner") + "ml");
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Logout")
            .setIcon(android.R.drawable.ic_menu_revert)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            
        // Only show admin panel for admin users
        if (currentUser != null && currentUser.isAdmin()) {
            menu.add(0, 2, 0, "Admin Panel")
                .setIcon(android.R.drawable.ic_menu_manage)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1: // Logout
                showLogoutDialog();
                return true;
            case 2: // Admin Panel
                if (currentUser != null && currentUser.isAdmin()) {
                    openAdminPanel();
                } else {
                    Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                currentUser = null;
                showLoginDialog();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void openAdminPanel() {
        Intent intent = new Intent(this, AdminActivity.class);
        intent.putExtra("current_user_id", currentUser.getUserId());
        startActivity(intent);
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}