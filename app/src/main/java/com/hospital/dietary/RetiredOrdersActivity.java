package com.hospital.dietary;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
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
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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
        datePickerButton = findViewById(R.id.datePickerButton);
        selectedDateText = findViewById(R.id.selectedDateText);
        retiredOrdersListView = findViewById(R.id.retiredOrdersListView);
        noRetiredOrdersText = findViewById(R.id.noRetiredOrdersText);
        printAllButton = findViewById(R.id.printAllButton);
        printSelectedButton = findViewById(R.id.printSelectedButton);

        // Set up list view for multiple selection
        retiredOrdersListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Create adapter
        retiredAdapter = new ArrayAdapter<Patient>(this, android.R.layout.simple_list_item_multiple_choice, retiredPatients) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
                }

                Patient patient = getItem(position);
                CheckedTextView textView = (CheckedTextView) convertView;

                String displayText = patient.getPatientFirstName() + " " + patient.getPatientLastName() +
                        " - " + patient.getWing() + " Room " + patient.getRoomNumber() +
                        "\nDiet: " + patient.getDiet() + " | " + patient.getMealCompletionStatus();

                textView.setText(displayText);
                return convertView;
            }
        };

        retiredOrdersListView.setAdapter(retiredAdapter);
    }

    private void setupListeners() {
        datePickerButton.setOnClickListener(v -> showDatePicker());

        if (printAllButton != null) {
            printAllButton.setOnClickListener(v -> printAllOrders());
        }

        if (printSelectedButton != null) {
            printSelectedButton.setOnClickListener(v -> printSelectedOrders());
        }

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
                    loadRetiredOrdersForDate();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set max date to yesterday (can't view future orders)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, -1);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.setTitle("Select Date to View Orders");
        datePickerDialog.show();
    }

    private void updateSelectedDateDisplay() {
        String dateString = dateFormat.format(selectedDate.getTime());
        selectedDateText.setText("Viewing orders for: " + dateString);
        datePickerButton.setText("📅 Change Date (" + dateFormatShort.format(selectedDate.getTime()) + ")");
    }

    private void loadRetiredOrdersForDate() {
        try {
            String selectedDateString = dbDateFormat.format(selectedDate.getTime());
            retiredPatients = patientDAO.getOrdersByDate(selectedDateString);

            if (retiredPatients.isEmpty()) {
                retiredOrdersListView.setVisibility(View.GONE);
                noRetiredOrdersText.setVisibility(View.VISIBLE);
                noRetiredOrdersText.setText("No orders found for " + dateFormat.format(selectedDate.getTime()) +
                        "\n\nTry selecting a different date.");

                if (printAllButton != null) printAllButton.setEnabled(false);
                if (printSelectedButton != null) printSelectedButton.setEnabled(false);
            } else {
                retiredOrdersListView.setVisibility(View.VISIBLE);
                noRetiredOrdersText.setVisibility(View.GONE);
                retiredAdapter.notifyDataSetChanged();

                if (printAllButton != null) printAllButton.setEnabled(true);
                if (printSelectedButton != null) printSelectedButton.setEnabled(true);
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        details.append("Location: ").append(patient.getWing()).append(" - Room ").append(patient.getRoomNumber()).append("\n");
        details.append("Diet: ").append(patient.getDiet()).append("\n");

        if (patient.getFluidRestriction() != null) {
            details.append("Fluid Restriction: ").append(patient.getFluidRestriction()).append("\n");
        }

        if (patient.getTextureModifications() != null) {
            details.append("Texture Modifications: ").append(patient.getTextureModifications()).append("\n");
        }

        details.append("\nMeal Status:\n");
        details.append("Breakfast: ").append(patient.isBreakfastNPO() ? "NPO" : "Complete").append("\n");
        details.append("Lunch: ").append(patient.isLunchNPO() ? "NPO" : "Complete").append("\n");
        details.append("Dinner: ").append(patient.isDinnerNPO() ? "NPO" : "Complete");

        new android.app.AlertDialog.Builder(this)
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
        printContent.append("Room: ").append(patient.getWing()).append(" - ").append(patient.getRoomNumber()).append("\n");
        printContent.append("Diet: ").append(patient.getDiet()).append("\n\n");

        // Add meal sections
        addMealSection(printContent, "BREAKFAST", patient.isBreakfastNPO(), patient.getDiet());
        addMealSection(printContent, "LUNCH", patient.isLunchNPO(), patient.getDiet());
        addMealSection(printContent, "DINNER", patient.isDinnerNPO(), patient.getDiet());

        // TODO: Implement actual printing functionality
        Toast.makeText(this, "Print Order - " + patient.getFullName() + " (" + dayOfWeek + " " + dateHeader + ")", Toast.LENGTH_LONG).show();
    }

    private void addMealSection(StringBuilder content, String mealName, boolean isNPO, String diet) {
        content.append("--- ").append(mealName).append(" ---\n");

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
        } else {
            content.append("Regular meal items\n");
            content.append("(Meal planning completed)\n\n");
        }
    }

    private void printAllOrders() {
        if (retiredPatients.isEmpty()) {
            Toast.makeText(this, "No orders to print", Toast.LENGTH_SHORT).show();
            return;
        }

        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
        String dateHeader = new SimpleDateFormat("M-d-yyyy", Locale.getDefault()).format(selectedDate.getTime());

        // TODO: Implement batch printing functionality
        Toast.makeText(this, "Print All Orders - " + retiredPatients.size() + " orders for " + dayOfWeek + " " + dateHeader, Toast.LENGTH_LONG).show();
    }

    private void printSelectedOrders() {
        android.util.SparseBooleanArray selectedItems = retiredOrdersListView.getCheckedItemPositions();
        List<Patient> selectedPatients = new ArrayList<>();

        for (int i = 0; i < selectedItems.size(); i++) {
            int position = selectedItems.keyAt(i);
            if (selectedItems.valueAt(i)) {
                selectedPatients.add(retiredPatients.get(position));
            }
        }

        if (selectedPatients.isEmpty()) {
            Toast.makeText(this, "No orders selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime());
        String dateHeader = new SimpleDateFormat("M-d-yyyy", Locale.getDefault()).format(selectedDate.getTime());

        // TODO: Implement selected printing functionality
        Toast.makeText(this, "Print Selected Orders - " + selectedPatients.size() + " orders for " + dayOfWeek + " " + dateHeader, Toast.LENGTH_LONG).show();
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
                loadRetiredOrdersForDate();
                Toast.makeText(this, "Orders refreshed for " + dateFormatShort.format(selectedDate.getTime()), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}