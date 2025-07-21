package com.hospital.dietary;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DocumentsActivity extends AppCompatActivity {

    // User data
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private TextView welcomeText;
    private Button productionSheetsButton;
    private Button galleyStockButton;
    private Button miscButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Documents");
        }

        // Get user data from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Initialize views
        initializeViews();

        // Update UI
        updateUI();

        // Set up listeners
        setupListeners();
    }

    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        productionSheetsButton = findViewById(R.id.productionSheetsButton);
        galleyStockButton = findViewById(R.id.galleyStockButton);
        miscButton = findViewById(R.id.miscButton);
        backButton = findViewById(R.id.backButton);
    }

    private void updateUI() {
        if (welcomeText != null && currentUserFullName != null) {
            welcomeText.setText("Documents - " + currentUserFullName);
        }
    }

    private void setupListeners() {
        productionSheetsButton.setOnClickListener(v -> openProductionSheets());
        galleyStockButton.setOnClickListener(v -> openGalleyStock());
        miscButton.setOnClickListener(v -> openMisc());

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void openProductionSheets() {
        // TODO: Implement Production Sheets functionality
        Toast.makeText(this, "Production Sheets - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void openGalleyStock() {
        // TODO: Implement Galley Stock functionality
        Toast.makeText(this, "Galley Stock - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void openMisc() {
        // TODO: Implement Misc functionality
        Toast.makeText(this, "Misc - Coming Soon!", Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}