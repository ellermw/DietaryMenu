package com.hospital.dietary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RetiredOrdersActivity extends AppCompatActivity {

    private static final String TAG = "RetiredOrdersActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components that may or may not exist in your layout
    private Button datePickerButton;
    private TextView selectedDateText;
    private ListView retiredOrdersListView;
    private TextView noRetiredOrdersText;
    private Button printAllButton;
    private Button printSelectedButton;

    // Date handling
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
    private SimpleDateFormat dateFormatShort = new SimpleDateFormat("M/d/yyyy", Locale.getDefault());

    private List<Patient> retiredPatients = new ArrayList<>();
    private ArrayAdapter<Patient> retiredAdapter;

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
        patientDAO = new PatientDAO(dbHelper);

        // Set title and enable up button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Retired Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize selected date to today
        selectedDate = Calendar.getInstance();
        selectedDate.add(Calendar.DAY_OF_MONTH, -1); // Default to yesterday

        initializeUI();
        setupListeners();
        updateSelectedDateDisplay();
        loadRetiredOrdersForDate();
    }

    private void initializeUI() {
        // Try to find UI elements with various possible IDs
        datePickerButton = findViewById(R.id.datePickerButton);
        if (datePickerButton == null) {
            datePickerButton = findViewById(R.id.selectDateButton);
        }

        selectedDateText = findViewById(R.id.selectedDateText);
        if (selectedDateText == null) {
            selectedDateText = findViewById(R.id.selectedDateTextView);
        }

        retiredOrdersListView = findViewById(R.id.retiredOrdersListView);
        if (retiredOrdersListView == null) {
            retiredOrdersListView = findViewById(R.id.ordersListView);
        }

        // Handle different possible IDs for the "no orders" text
        noRetiredOrdersText = findViewById(R.id.noRetiredOrdersText);
        if (noRetiredOrdersText == null) {
            noRetiredOrdersText = findViewById(R.id.noOrdersText);
        }
        if (noRetiredOrdersText == null) {
            noRetiredOrdersText = findViewById(R.id.noDataText);
        }

        printAllButton = findViewById(R.id.printAllButton);
        printSelectedButton = findViewById(R.id.printSelectedButton);

        // Set up list view for multiple selection if available
        if (retiredOrdersListView != null) {
            retiredOrdersListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            // Create adapter
            retiredAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_multiple_choice, retiredPatients) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    Patient patient = getItem(position);
                    TextView textView = view.findViewById(android.R.id.text1);

                    if (patient != null && textView != null) {
                        // FIXED: Show individual meal diets with proper ADA display
                        String breakfastDiet = getCleanDietDisplay(patient.getBreakfastDiet(), patient.isBreakfastAda());
                        String lunchDiet = getCleanDietDisplay(patient.getLunchDiet(), patient.isLunchAda());
                        String dinnerDiet = getCleanDietDisplay(patient.getDinnerDiet(), patient.isDinnerAda());

                        String displayText = String.format("%s - %s\nBreakfast: %s | Lunch: %s | Dinner: %s",
                                patient.getFullName(),
                                patient.getLocationInfo(),
                                breakfastDiet, lunchDiet, dinnerDiet);

                        textView.setText(displayText);
                    }

                    return view;
                }
            };

            retiredOrdersListView.setAdapter(retiredAdapter);
        } else {
            Log.e(TAG, "retiredOrdersListView not found in layout");
        }
    }

    // FIXED: Helper method to clean up diet display and avoid duplicate (ADA)
    private String getCleanDietDisplay(String diet, boolean isAda) {
        if (diet == null) return "Regular";

        String cleanDiet;
        if (diet.contains("(ADA)") || isAda) {
            // If diet already contains (ADA) or isAda is true, format it properly
            if (diet.contains("(ADA)")) {
                // Diet already has (ADA) in it, use as is
                cleanDiet = diet;
            } else {
                // isAda is true but diet doesn't contain (ADA), add it once
                cleanDiet = diet + " (ADA)";
            }
        } else {
            // Not an ADA diet, use diet as is
            cleanDiet = diet;
        }

        return cleanDiet;
    }

    private void setupListeners() {
        if (datePickerButton != null) {
            datePickerButton.setOnClickListener(v -> showDatePicker());
        } else {
            Log.w(TAG, "datePickerButton not found in layout");
        }

        if (printAllButton != null) {
            printAllButton.setOnClickListener(v -> printAllOrders());
        } else {
            Log.w(TAG, "printAllButton not found in layout");
        }

        if (printSelectedButton != null) {
            printSelectedButton.setOnClickListener(v -> printSelectedOrders());
        } else {
            Log.w(TAG, "printSelectedButton not found in layout");
        }

        if (retiredOrdersListView != null) {
            retiredOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
                if (position < retiredPatients.size()) {
                    Patient patient = retiredPatients.get(position);
                    showOrderDetails(patient);
                }
            });
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateSelectedDateDisplay();
                    loadRetiredOrdersForDate();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateSelectedDateDisplay() {
        if (selectedDateText != null) {
            String dateString = dateFormat.format(selectedDate.getTime());
            selectedDateText.setText("Viewing orders for: " + dateString);
        }
    }

    private void loadRetiredOrdersForDate() {
        try {
            // For this demo, show completed patients as "retired" orders
            retiredPatients.clear();
            retiredPatients.addAll(patientDAO.getCompletedPatients());

            if (retiredPatients.isEmpty()) {
                if (retiredOrdersListView != null) {
                    retiredOrdersListView.setVisibility(View.GONE);
                }
                if (noRetiredOrdersText != null) {
                    noRetiredOrdersText.setVisibility(View.VISIBLE);

                    String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
                    String dateStr = new SimpleDateFormat("M-d-yyyy", Locale.getDefault()).format(selectedDate.getTime());
                    noRetiredOrdersText.setText("📋 No completed orders found for " + dayOfWeek + "\n" + dateStr + "\n\nTry choosing a different date from the calendar.");
                }

                if (printAllButton != null) printAllButton.setEnabled(false);
                if (printSelectedButton != null) printSelectedButton.setEnabled(false);
            } else {
                if (retiredOrdersListView != null) {
                    retiredOrdersListView.setVisibility(View.VISIBLE);
                }
                if (noRetiredOrdersText != null) {
                    noRetiredOrdersText.setVisibility(View.GONE);
                }
                if (retiredAdapter != null) {
                    retiredAdapter.notifyDataSetChanged();
                }

                if (printAllButton != null) printAllButton.setEnabled(true);
                if (printSelectedButton != null) printSelectedButton.setEnabled(true);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error loading orders", e);
        }
    }

    private void showOrderDetails(Patient patient) {
        StringBuilder details = new StringBuilder();

        // Add date header with day of week
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
        String dateHeader = new SimpleDateFormat("M/d/yyyy", Locale.getDefault()).format(selectedDate.getTime());

        details.append("=== ").append(dayOfWeek).append(" ===\n");
        details.append(dateHeader).append("\n\n");

        details.append("Patient: ").append(patient.getFullName()).append("\n");
        details.append("Location: ").append(patient.getWing()).append(" - Room ").append(patient.getRoomNumber()).append("\n\n");

        // FIXED: Show individual meal diets with proper formatting
        details.append("BREAKFAST - ").append(getCleanDietDisplay(patient.getBreakfastDiet(), patient.isBreakfastAda())).append("\n");
        details.append("Status: ").append(patient.isBreakfastNPO() ? "NPO" : "Complete").append("\n\n");

        details.append("LUNCH - ").append(getCleanDietDisplay(patient.getLunchDiet(), patient.isLunchAda())).append("\n");
        details.append("Status: ").append(patient.isLunchNPO() ? "NPO" : "Complete").append("\n\n");

        details.append("DINNER - ").append(getCleanDietDisplay(patient.getDinnerDiet(), patient.isDinnerAda())).append("\n");
        details.append("Status: ").append(patient.isDinnerNPO() ? "NPO" : "Complete");

        if (patient.getFluidRestriction() != null) {
            details.append("\n\nFluid Restriction: ").append(patient.getFluidRestriction());
        }

        if (patient.getTextureModifications() != null && !patient.getTextureModifications().equals("Regular")) {
            details.append("\nTexture Modifications: ").append(patient.getTextureModifications());
        }

        new AlertDialog.Builder(this)
                .setTitle("Order Details")
                .setMessage(details.toString())
                .setPositiveButton("Print This Order", (dialog, which) -> printSingleOrder(patient))
                .setNegativeButton("Close", null)
                .show();
    }

    private void printSingleOrder(Patient patient) {
        // Create print content with proper date formatting
        StringBuilder printContent = new StringBuilder();

        // Add date header for printed menu
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
        String dateHeader = new SimpleDateFormat("M-d-yyyy", Locale.getDefault()).format(selectedDate.getTime());

        printContent.append("========================================\n");
        printContent.append("         DIETARY MENU\n");
        printContent.append("========================================\n");
        printContent.append("        ").append(dayOfWeek).append("\n");
        printContent.append("        ").append(dateHeader).append("\n");
        printContent.append("========================================\n\n");

        printContent.append("Patient: ").append(patient.getFullName()).append("\n");
        printContent.append("Room: ").append(patient.getWing()).append(" - ").append(patient.getRoomNumber()).append("\n\n");

        // Add meal sections with individual diets
        addMealSection(printContent, "BREAKFAST", patient.isBreakfastNPO(),
                getCleanDietDisplay(patient.getBreakfastDiet(), patient.isBreakfastAda()));
        addMealSection(printContent, "LUNCH", patient.isLunchNPO(),
                getCleanDietDisplay(patient.getLunchDiet(), patient.isLunchAda()));
        addMealSection(printContent, "DINNER", patient.isDinnerNPO(),
                getCleanDietDisplay(patient.getDinnerDiet(), patient.isDinnerAda()));

        // TODO: Implement actual printing functionality
        Toast.makeText(this, "Print Order - " + patient.getFullName() + " (" + dayOfWeek + " " + dateHeader + ")", Toast.LENGTH_LONG).show();
    }

    private void addMealSection(StringBuilder content, String mealName, boolean isNPO, String diet) {
        content.append("--- ").append(mealName).append(" ---\n");
        content.append("Diet: ").append(diet).append("\n");

        if (isNPO) {
            content.append("NPO (Nothing by mouth)\n\n");
        } else if (diet.startsWith("Clear Liquid")) {
            // Add clear liquid items with ADA substitutions if needed
            boolean isADA = diet.contains("ADA");
            if (isADA) {
                content.append("• Apple Juice (ADA)\n");
                content.append("• Sprite Zero (ADA)\n");
                content.append("• Sugar Free Jello (ADA)\n");
            } else {
                content.append("• Orange Juice\n");
                content.append("• Sprite\n");
                content.append("• Jello\n");
            }
            content.append("• Clear Broth\n");
            content.append("• Water\n");
            content.append("• Tea/Coffee\n\n");
        } else if (diet.startsWith("Full Liquid")) {
            // Add full liquid items with ADA substitutions if needed
            boolean isADA = diet.contains("ADA");

            switch (mealName) {
                case "BREAKFAST":
                    content.append("• Apple Juice (120ml)\n");
                    content.append("• Jello").append(isADA ? " (Sugar Free)" : "").append("\n");
                    content.append("• Cream of Wheat\n");
                    content.append("• Coffee (200ml)\n");
                    content.append("• ").append(isADA ? "2% Milk (240ml)" : "Whole Milk (240ml)").append("\n");
                    content.append("• ").append(isADA ? "Sprite Zero (355ml)" : "Sprite (355ml)").append("\n");
                    content.append("• Ensure (240ml)\n\n");
                    break;

                case "LUNCH":
                    content.append("• Cranberry Juice (120ml)\n");
                    content.append("• Jello").append(isADA ? " (Sugar Free)" : "").append("\n");
                    content.append("• Cream of Chicken Soup\n");
                    content.append("• Pudding").append(isADA ? " (Sugar Free)" : "").append("\n");
                    content.append("• ").append(isADA ? "2% Milk (240ml)" : "Whole Milk (240ml)").append("\n");
                    content.append("• ").append(isADA ? "Sprite Zero (355ml)" : "Sprite (355ml)").append("\n");
                    content.append("• Ensure (240ml)\n\n");
                    break;

                case "DINNER":
                    content.append("• Apple Juice (120ml)\n");
                    content.append("• Jello").append(isADA ? " (Sugar Free)" : "").append("\n");
                    content.append("• Tomato Soup\n");
                    content.append("• Pudding").append(isADA ? " (Sugar Free)" : "").append("\n");
                    content.append("• ").append(isADA ? "2% Milk (240ml)" : "Whole Milk (240ml)").append("\n");
                    content.append("• ").append(isADA ? "Sprite Zero (355ml)" : "Sprite (355ml)").append("\n");
                    content.append("• Ensure (240ml)\n\n");
                    break;
            }

        } else if (diet.startsWith("Puree")) {
            // Add puree diet items
            switch (mealName) {
                case "BREAKFAST":
                    content.append("• Pureed Scrambled Eggs\n");
                    content.append("• Pureed Oatmeal\n");
                    content.append("• Apple Juice\n");
                    content.append("• Coffee\n\n");
                    break;
                case "LUNCH":
                    content.append("• Pureed Chicken\n");
                    content.append("• Pureed Potatoes\n");
                    content.append("• Pureed Vegetables\n");
                    content.append("• Thickened Liquids\n\n");
                    break;
                case "DINNER":
                    content.append("• Pureed Beef\n");
                    content.append("• Pureed Rice\n");
                    content.append("• Pureed Carrots\n");
                    content.append("• Pudding").append(diet.contains("ADA") ? " (Sugar Free)" : "").append("\n\n");
                    break;
            }
        } else {
            // Regular diet or other diets
            content.append("Standard items for ").append(diet).append(" diet\n\n");
        }
    }

    private void printAllOrders() {
        if (retiredPatients.isEmpty()) {
            Toast.makeText(this, "No orders to print", Toast.LENGTH_SHORT).show();
            return;
        }

        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
        String dateHeader = new SimpleDateFormat("M-d-yyyy", Locale.getDefault()).format(selectedDate.getTime());

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Print All Orders")
                .setMessage("Print all " + retiredPatients.size() + " orders for " + dayOfWeek + " " + dateHeader + "?")
                .setPositiveButton("Print", (dialog, which) -> {
                    // TODO: Implement actual printing functionality
                    Toast.makeText(this, "Print All Orders - " + retiredPatients.size() + " orders (" + dayOfWeek + " " + dateHeader + ")", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void printSelectedOrders() {
        if (retiredOrdersListView == null) {
            Toast.makeText(this, "List view not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIXED: Added proper import for SparseBooleanArray
        SparseBooleanArray checkedItems = retiredOrdersListView.getCheckedItemPositions();
        List<Patient> selectedPatients = new ArrayList<>();

        for (int i = 0; i < checkedItems.size(); i++) {
            int position = checkedItems.keyAt(i);
            if (checkedItems.valueAt(i)) {
                if (position < retiredPatients.size()) {
                    selectedPatients.add(retiredPatients.get(position));
                }
            }
        }

        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No orders selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
        String dateHeader = new SimpleDateFormat("M-d-yyyy", Locale.getDefault()).format(selectedDate.getTime());

        // TODO: Implement actual printing functionality
        Toast.makeText(this, "Print Selected Orders - " + selectedPatients.size() + " orders (" + dayOfWeek + " " + dateHeader + ")", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Skip menu inflation if menu file doesn't exist
        try {
            getMenuInflater().inflate(R.menu.menu_retired_orders, menu);
        } catch (Exception e) {
            Log.d(TAG, "Menu file not found, skipping");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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