// ================================================================================================
// FILE: app/src/main/java/com/hospital/dietary/ViewOrdersActivity.java
// ================================================================================================

package com.hospital.dietary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.hospital.dietary.dao.FinalizedOrderDAO;
import com.hospital.dietary.models.FinalizedOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewOrdersActivity extends AppCompatActivity {
    
    private DatabaseHelper dbHelper;
    private FinalizedOrderDAO finalizedOrderDAO;
    private ListView ordersListView;
    private Spinner dateFilterSpinner;
    private Button printSelectedButton, printAllButton, backButton;
    private OrderAdapter orderAdapter;
    private List<FinalizedOrder> allOrders = new ArrayList<>();
    private List<FinalizedOrder> filteredOrders = new ArrayList<>();
    private List<String> availableDates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_orders);
        
        initializeDatabase();
        initializeUI();
        setupListeners();
        loadOrders();
    }
    
    private void initializeDatabase() {
        dbHelper = new DatabaseHelper(this);
        finalizedOrderDAO = new FinalizedOrderDAO(dbHelper);
    }
    
    private void initializeUI() {
        ordersListView = findViewById(R.id.ordersListView);
        dateFilterSpinner = findViewById(R.id.dateFilterSpinner);
        printSelectedButton = findViewById(R.id.printSelectedButton);
        printAllButton = findViewById(R.id.printAllButton);
        backButton = findViewById(R.id.backButton);
        
        // Setup adapter
        orderAdapter = new OrderAdapter(this, filteredOrders);
        ordersListView.setAdapter(orderAdapter);
        ordersListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }
    
    private void setupListeners() {
        dateFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterOrdersByDate();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        printSelectedButton.setOnClickListener(v -> printSelectedOrders());
        printAllButton.setOnClickListener(v -> printAllOrders());
        backButton.setOnClickListener(v -> finish());
    }
    
    private void loadOrders() {
        // Load all orders
        allOrders = finalizedOrderDAO.getAllFinalizedOrders();
        
        // Load available dates
        availableDates = finalizedOrderDAO.getDistinctOrderDates();
        availableDates.add(0, "All Dates");
        
        // Setup date filter spinner
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, availableDates);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateFilterSpinner.setAdapter(dateAdapter);
        
        // Initially show all orders
        filteredOrders.clear();
        filteredOrders.addAll(allOrders);
        orderAdapter.notifyDataSetChanged();
        
        updateButtonStates();
    }
    
    private void filterOrdersByDate() {
        String selectedDate = (String) dateFilterSpinner.getSelectedItem();
        filteredOrders.clear();
        
        if (selectedDate == null || selectedDate.equals("All Dates")) {
            filteredOrders.addAll(allOrders);
        } else {
            for (FinalizedOrder order : allOrders) {
                if (order.getOrderDate().equals(selectedDate)) {
                    filteredOrders.add(order);
                }
            }
        }
        
        orderAdapter.notifyDataSetChanged();
        updateButtonStates();
        
        // Clear all selections when filter changes
        ordersListView.clearChoices();
        for (int i = 0; i < ordersListView.getChildCount(); i++) {
            ordersListView.setItemChecked(i, false);
        }
    }
    
    private void updateButtonStates() {
        boolean hasOrders = !filteredOrders.isEmpty();
        printAllButton.setEnabled(hasOrders);
        updatePrintSelectedButton();
    }
    
    private void updatePrintSelectedButton() {
        int selectedCount = ordersListView.getCheckedItemCount();
        printSelectedButton.setEnabled(selectedCount > 0);
        printSelectedButton.setText("Print Selected (" + selectedCount + ")");
    }
    
    private void printSelectedOrders() {
        List<FinalizedOrder> selectedOrders = new ArrayList<>();
        SparseBooleanArray checked = ordersListView.getCheckedItemPositions();
        
        for (int i = 0; i < checked.size(); i++) {
            if (checked.valueAt(i)) {
                selectedOrders.add(filteredOrders.get(checked.keyAt(i)));
            }
        }
        
        if (!selectedOrders.isEmpty()) {
            generateAndPrint(selectedOrders);
        }
    }
    
    private void printAllOrders() {
        if (!filteredOrders.isEmpty()) {
            generateAndPrint(filteredOrders);
        }
    }
    
    private void generateAndPrint(List<FinalizedOrder> ordersToPrint) {
        String html = generatePrintHTML(ordersToPrint);
        
        WebView webView = new WebView(this);
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        
        // Create print job
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        String jobName = "Dietary Orders - " + ordersToPrint.size() + " orders";
        
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
        
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(PrintAttributes.MediaSize.NA_LETTER);
        builder.setResolution(new PrintAttributes.Resolution("id", PRINT_SERVICE, 300, 300));
        builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
        builder.setColorMode(PrintAttributes.COLOR_MODE_MONOCHROME);
        
        printManager.print(jobName, printAdapter, builder.build());
    }
    
    private String generatePrintHTML(List<FinalizedOrder> orders) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html><html><head>");
        html.append("<style>");
        html.append("@page { size: landscape; margin: 0.5in; }");
        html.append("body { font-family: Arial, sans-serif; font-size: 8pt; margin: 0; }");
        html.append(".order-container { display: flex; width: 100%; border: 2px solid #000; margin-bottom: 10px; page-break-inside: avoid; }");
        html.append(".meal-section { flex: 1; border-right: 1px solid #000; padding: 5px; }");
        html.append(".meal-section:last-child { border-right: none; }");
        html.append(".meal-header { background-color: #f0f0f0; font-weight: bold; text-align: center; padding: 3px; border-bottom: 1px solid #000; }");
        html.append(".patient-info { background-color: #e8e8e8; padding: 3px; margin-bottom: 3px; font-size: 7pt; }");
        html.append(".diet-info { background-color: #f8f8f8; padding: 2px; margin-bottom: 3px; font-size: 6pt; }");
        html.append(".items-list { font-size: 7pt; line-height: 1.2; }");
        html.append(".items-list div { margin-bottom: 1px; }");
        html.append(".cut-line { border-top: 1px dashed #666; margin: 2px 0; }");
        html.append("</style>");
        html.append("</head><body>");
        
        for (FinalizedOrder order : orders) {
            html.append("<div class='order-container'>");
            
            // Breakfast Section
            html.append("<div class='meal-section'>");
            html.append("<div class='meal-header'>BREAKFAST</div>");
            appendPatientInfo(html, order);
            appendMealItems(html, "Breakfast", order.getBreakfastItems(), order.getBreakfastJuices(), order.getBreakfastDrinks());
            html.append("</div>");
            
            // Lunch Section
            html.append("<div class='meal-section'>");
            html.append("<div class='meal-header'>LUNCH</div>");
            appendPatientInfo(html, order);
            appendMealItems(html, "Lunch", order.getLunchItems(), order.getLunchJuices(), order.getLunchDrinks());
            html.append("</div>");
            
            // Dinner Section
            html.append("<div class='meal-section'>");
            html.append("<div class='meal-header'>DINNER</div>");
            appendPatientInfo(html, order);
            appendMealItems(html, "Dinner", order.getDinnerItems(), order.getDinnerJuices(), order.getDinnerDrinks());
            html.append("</div>");
            
            html.append("</div>");
        }
        
        html.append("</body></html>");
        return html.toString();
    }
    
    private void appendPatientInfo(StringBuilder html, FinalizedOrder order) {
        html.append("<div class='patient-info'>");
        html.append("<strong>").append(order.getPatientName()).append("</strong><br>");
        html.append(order.getWing()).append(" - Room ").append(order.getRoom()).append("<br>");
        html.append("Date: ").append(order.getOrderDate());
        html.append("</div>");
        
        html.append("<div class='diet-info'>");
        html.append("<strong>Diet:</strong> ").append(order.getDietType()).append("<br>");
        if (order.getFluidRestriction() != null && !order.getFluidRestriction().equals("No Restriction")) {
            html.append("<strong>Fluid:</strong> ").append(order.getFluidRestriction()).append("<br>");
        }
        String textures = order.getTextureModificationsString();
        if (!textures.equals("None")) {
            html.append("<strong>Texture:</strong> ").append(textures);
        }
        html.append("</div>");
    }
    
    private void appendMealItems(StringBuilder html, String mealType, List<String> items, List<String> juices, List<String> drinks) {
        html.append("<div class='items-list'>");
        
        // Main items
        for (String item : items) {
            if (item != null && !item.trim().isEmpty()) {
                html.append("<div>• ").append(item).append("</div>");
            }
        }
        
        // Juices
        for (String juice : juices) {
            if (juice != null && !juice.trim().isEmpty()) {
                html.append("<div>• ").append(juice).append("</div>");
            }
        }
        
        // Drinks
        for (String drink : drinks) {
            if (drink != null && !drink.trim().isEmpty()) {
                html.append("<div>• ").append(drink).append("</div>");
            }
        }
        
        html.append("</div>");
    }
    
    // Custom adapter for orders list
    private class OrderAdapter extends ArrayAdapter<FinalizedOrder> {
        public OrderAdapter(Context context, List<FinalizedOrder> orders) {
            super(context, 0, orders);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            }
            
            FinalizedOrder order = getItem(position);
            CheckedTextView textView = (CheckedTextView) convertView;
            
            String displayText = order.getOrderDate() + " | " + order.getWing() + " - Room " + order.getRoom() + 
                               " | " + order.getPatientName() + " (" + order.getDietType() + ")";
            textView.setText(displayText);
            
            // Update print button when items are checked/unchecked
            textView.setOnClickListener(v -> {
                updatePrintSelectedButton();
            });
            
            return convertView;
        }
    }
    
    @Override
    protected void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}