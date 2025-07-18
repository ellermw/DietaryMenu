package com.hospital.dietary;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.FinalizedOrderDAO;
import com.hospital.dietary.models.FinalizedOrder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RetiredOrdersActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private FinalizedOrderDAO finalizedOrderDAO;
    
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;
    
    private ListView retiredOrdersListView;
    private TextView noRetiredOrdersText;
    private TextView retiredOrdersCountText;
    private Button backButton;
    private Button refreshButton;
    
    private List<FinalizedOrder> retiredOrders = new ArrayList<>();
    private RetiredOrdersAdapter adapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retired_orders);
        
        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");
        
        // Initialize database
        dbHelper = new DatabaseHelper(this);
        finalizedOrderDAO = new FinalizedOrderDAO(dbHelper);
        
        initializeUI();
        setupListeners();
        loadRetiredOrders();
    }
    
    private void initializeUI() {
        retiredOrdersListView = findViewById(R.id.retiredOrdersListView);
        noRetiredOrdersText = findViewById(R.id.noRetiredOrdersText);
        retiredOrdersCountText = findViewById(R.id.retiredOrdersCountText);
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshButton);
        
        setTitle("Retired Orders");
        
        // Setup adapter
        adapter = new RetiredOrdersAdapter(this, retiredOrders);
        retiredOrdersListView.setAdapter(adapter);
    }
    
    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());
        
        refreshButton.setOnClickListener(v -> {
            loadRetiredOrders();
            Toast.makeText(this, "Retired orders refreshed", Toast.LENGTH_SHORT).show();
        });
        
        // Order selection for detailed view
        retiredOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
            FinalizedOrder selectedOrder = retiredOrders.get(position);
            showOrderDetails(selectedOrder);
        });
    }
    
    private void loadRetiredOrders() {
        try {
            // Calculate the cutoff date (6 days ago)
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -6);
            String cutoffDate = dateFormat.format(calendar.getTime());
            
            // Get all orders older than 6 days
            retiredOrders = finalizedOrderDAO.getRetiredOrders(cutoffDate);
            
            // Sort by date (most recent first), then by wing, then by room
            Collections.sort(retiredOrders, new Comparator<FinalizedOrder>() {
                @Override
                public int compare(FinalizedOrder o1, FinalizedOrder o2) {
                    // First by date (most recent first)
                    int dateCompare = o2.getOrderDate().compareTo(o1.getOrderDate());
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    
                    // Then by wing
                    int wingCompare = o1.getWing().compareTo(o2.getWing());
                    if (wingCompare != 0) {
                        return wingCompare;
                    }
                    
                    // Finally by room number
                    try {
                        int room1 = Integer.parseInt(o1.getRoom());
                        int room2 = Integer.parseInt(o2.getRoom());
                        return Integer.compare(room1, room2);
                    } catch (NumberFormatException e) {
                        return o1.getRoom().compareTo(o2.getRoom());
                    }
                }
            });
            
            updateUI();
            
        } catch (Exception e) {
            Toast.makeText(this, "Error loading retired orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateUI() {
        if (retiredOrders.isEmpty()) {
            retiredOrdersListView.setVisibility(View.GONE);
            noRetiredOrdersText.setVisibility(View.VISIBLE);
            retiredOrdersCountText.setVisibility(View.GONE);
            noRetiredOrdersText.setText("No retired orders found");
        } else {
            retiredOrdersListView.setVisibility(View.VISIBLE);
            noRetiredOrdersText.setVisibility(View.GONE);
            retiredOrdersCountText.setVisibility(View.VISIBLE);
            retiredOrdersCountText.setText(retiredOrders.size() + " retired order(s)");
        }
        
        adapter.notifyDataSetChanged();
    }
    
    private void showOrderDetails(FinalizedOrder order) {
        // Create a detailed view dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Retired Order Details");
        
        StringBuilder details = new StringBuilder();
        details.append("Patient: ").append(order.getPatientName()).append("\n");
        details.append("Location: ").append(order.getWing()).append(" - Room ").append(order.getRoom()).append("\n");
        details.append("Date: ").append(order.getOrderDate()).append("\n");
        details.append("Diet: ").append(order.getDietType()).append("\n");
        
        if (order.getFluidRestriction() != null && !order.getFluidRestriction().isEmpty()) {
            details.append("Fluid Restriction: ").append(order.getFluidRestriction()).append("\n");
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
}