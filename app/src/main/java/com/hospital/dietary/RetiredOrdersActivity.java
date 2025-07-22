package com.hospital.dietary;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;
import java.util.Locale;

public class RetiredOrdersActivity extends AppCompatActivity {

    private static final String TAG = "RetiredOrdersActivity";

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Button selectDateButton, printAllButton;
    private TextView selectedDateTextView, noOrdersText;
    private ListView retiredOrdersListView;

    private Calendar selectedDate;
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

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Retired Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeViews();
        setupListeners();

        // Set today as default selected date
        selectedDate = Calendar.getInstance();
        updateSelectedDateDisplay();
        loadRetiredOrders();
    }

    private void initializeViews() {
        selectDateButton = findViewById(R.id.selectDateButton);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
        retiredOrdersListView = findViewById(R.id.retiredOrdersListView);
        noOrdersText = findViewById(R.id.noOrdersText);
        printAllButton = findViewById(R.id.printAllButton);
    }

    private void setupListeners() {
        selectDateButton.setOnClickListener(v -> showDatePicker());
        printAllButton.setOnClickListener(v -> printAllOrders());

        retiredOrdersListView.setOnItemClickListener((parent, view, position, id) -> {
            Patient patient = retiredPatients.get(position);
            showOrderDetails(patient);
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateSelectedDateDisplay();
                    loadRetiredOrders();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void updateSelectedDateDisplay() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());
        String dateString = displayFormat.format(selectedDate.getTime());
        selectedDateTextView.setText("Selected Date: " + dateString);
    }

    private void loadRetiredOrders() {
        try {
            // For this demo, show completed patients as "retired" orders
            retiredPatients.clear();
            retiredPatients.addAll(patientDAO.getCompletedPatients());

            if (retiredPatients.isEmpty()) {
                retiredOrdersListView.setVisibility(View.GONE);
                noOrdersText.setVisibility(View.VISIBLE);
                printAllButton.setVisibility(View.GONE);

                String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
                String dateStr = new SimpleDateFormat("M-d-yyyy", Locale.getDefault()).format(selectedDate.getTime());
                noOrdersText.setText("No completed orders found for " + dayOfWeek + "\n" + dateStr);
            } else {
                retiredOrdersListView.setVisibility(View.VISIBLE);
                noOrdersText.setVisibility(View.GONE);
                printAllButton.setVisibility(View.VISIBLE);

                // Create adapter for retired orders
                retiredAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_2, android.R.id.text1, retiredPatients) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        Patient patient = getItem(position);
                        TextView text1 = view.findViewById(android.R.id.text1);
                        TextView text2 = view.findViewById(android.R.id.text2);

                        if (patient != null) {
                            text1.setText(patient.getFullName());

                            // FIXED: Show individual meal diets with proper ADA display
                            String breakfastDiet = getCleanDietDisplay(patient.getBreakfastDiet(), patient.isBreakfastAda());
                            String lunchDiet = getCleanDietDisplay(patient.getLunchDiet(), patient.isLunchAda());
                            String dinnerDiet = getCleanDietDisplay(patient.getDinnerDiet(), patient.isDinnerAda());

                            String mealInfo = String.format("Breakfast - %s • Lunch - %s • Dinner - %s",
                                    breakfastDiet, lunchDiet, dinnerDiet);

                            text2.setText(patient.getLocationInfo() + "\n" + mealInfo);
                        }

                        return view;
                    }
                };

                retiredOrdersListView.setAdapter(retiredAdapter);
            }

            Log.d(TAG, "Loaded " + retiredPatients.size() + " retired orders");

        } catch (Exception e) {
            Toast.makeText(this, "Error loading retired orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error loading retired orders", e);
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

    private void showOrderDetails(Patient patient) {
        StringBuilder details = new StringBuilder();

        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
        String dateHeader = new SimpleDateFormat("M-d-yyyy", Locale.getDefault()).format(selectedDate.getTime());

        details.append("Patient: ").append(patient.getFullName()).append("\n");
        details.append("Room: ").append(patient.getLocationInfo()).append("\n");
        details.append("Date: ").append(dayOfWeek).append(" ").append(dateHeader).append("\n\n");

        // FIXED: Show individual meal diets with proper formatting
        details.append("BREAKFAST - ").append(getCleanDietDisplay(patient.getBreakfastDiet(), patient.isBreakfastAda())).append("\n");
        details.append("Status: ").append(patient.isBreakfastNPO() ? "NPO" : "Complete").append("\n\n");

        details.append("LUNCH - ").append(getCleanDietDisplay(patient.getLunchDiet(), patient.isLunchAda())).append("\n");
        details.append("Status: ").append(patient.isLunchNPO() ? "NPO" : "Complete").append("\n\n");

        details.append("DINNER - ").append(getCleanDietDisplay(patient.getDinnerDiet(), patient.isDinnerAda())).append("\n");
        details.append("Status: ").append(patient.isDinnerNPO() ? "NPO" : "Complete");

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_retired_orders, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                loadRetiredOrders();
                return true;
            case R.id.action_print_all:
                printAllOrders();
                return true;
            case R.id.action_select_date:
                showDatePicker();
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