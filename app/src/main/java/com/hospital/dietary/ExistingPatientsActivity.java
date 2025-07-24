package com.hospital.dietary;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.hospital.dietary.dao.PatientDAO;
import com.hospital.dietary.models.Patient;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExistingPatientsActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private PatientDAO patientDAO;

    // User information
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private Toolbar toolbar;
    private EditText searchInput;
    private Spinner dayFilterSpinner;
    private TextView patientsCountText;
    private ListView patientsListView;
    private LinearLayout bulkOperationsContainer;
    private CheckBox selectAllCheckBox;
    private Button printMenusButton;
    private Button deleteSelectedButton;

    // Data
    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> filteredPatients = new ArrayList<>();
    private PatientAdapter patientsAdapter;
    private DayFilterAdapter dayFilterAdapter;

    // Date tracking
    private int todayIndex = 6; // Today's position in the 7-day list
    private List<String> dayLabels = new ArrayList<>();
    private List<Date> dayDates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_patients);

        // Get user information from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        patientDAO = new PatientDAO(dbHelper);

        // Setup toolbar
        setupToolbar();

        // Initialize UI
        initializeUI();

        // Setup listeners
        setupListeners();

        // Load patients
        loadPatients();
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Existing Patients");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeUI() {
        searchInput = findViewById(R.id.searchInput);
        dayFilterSpinner = findViewById(R.id.dayFilterSpinner);
        patientsCountText = findViewById(R.id.patientsCountText);
        patientsListView = findViewById(R.id.patientsListView);
        bulkOperationsContainer = findViewById(R.id.bulkOperationsContainer);
        selectAllCheckBox = findViewById(R.id.selectAllCheckBox);
        printMenusButton = findViewById(R.id.printMenusButton);
        deleteSelectedButton = findViewById(R.id.deleteSelectedButton);

        // Setup day filter with date range
        setupDayFilter();

        // Initialize list view with CHOICE_MODE_NONE for clickable items
        patientsListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
    }

    private void setupDayFilter() {
        dayLabels.clear();
        dayDates.clear();

        // Always start with "All Days"
        dayLabels.add("All Days");
        dayDates.add(null);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.getDefault());

        // Move to 5 days ago
        calendar.add(Calendar.DAY_OF_YEAR, -5);

        // Add previous 5 days, today, and tomorrow
        for (int i = 0; i < 7; i++) {
            Date date = calendar.getTime();
            String dayName = dayFormat.format(date);
            String dateStr = dateFormat.format(date);

            if (i == 5) { // Today
                dayLabels.add("Today - " + dateStr);
                todayIndex = dayLabels.size() - 1;
            } else {
                dayLabels.add(dayName + " - " + dateStr);
            }

            dayDates.add(date);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Create and set adapter
        dayFilterAdapter = new DayFilterAdapter(dayLabels.toArray(new String[0]));
        dayFilterSpinner.setAdapter(dayFilterAdapter);

        // Set default selection to Today
        dayFilterSpinner.setSelection(todayIndex);
    }