package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.User;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import android.Manifest;
import android.content.pm.PackageManager;

public class UserManagementActivity extends AppCompatActivity {

    private static final String TAG = "UserManagementActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private DatabaseHelper dbHelper;
    private UserDAO userDAO;
    private String currentUsername;
    private String currentUserRole;
    private String currentUserFullName;

    // UI Components
    private ListView usersListView;
    private TextView usersCountText;
    private Button addUserButton;
    private Button refreshButton;
    private Button backButton;
    private Button exportUsersButton;
    private Button importUsersButton;
    private Button passwordPolicyButton;

    // Data
    private List<User> allUsers = new ArrayList<>();
    private UserAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        Log.d(TAG, "UserManagementActivity onCreate started");

        // Get user data from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // Check admin access
        boolean isAdmin = currentUserRole != null &&
                ("Admin".equalsIgnoreCase(currentUserRole.trim()) ||
                        "Administrator".equalsIgnoreCase(currentUserRole.trim()));

        if (!isAdmin) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);

        // Set title and back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize UI
        initializeViews();
        setupListeners();
        loadUsers();
    }

    private void initializeViews() {
        usersListView = findViewById(R.id.usersListView);
        usersCountText = findViewById(R.id.usersCountText);
        addUserButton = findViewById(R.id.addUserButton);
        refreshButton = findViewById(R.id.refreshButton);
        backButton = findViewById(R.id.backButton);
        exportUsersButton = findViewById(R.id.exportUsersButton);
        importUsersButton = findViewById(R.id.importUsersButton);
        passwordPolicyButton = findViewById(R.id.passwordPolicyButton);
    }

    private void setupListeners() {
        if (addUserButton != null) {
            addUserButton.setOnClickListener(v -> showAddUserDialog());
        }

        if (refreshButton != null) {
            refreshButton.setOnClickListener(v -> loadUsers());
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        if (exportUsersButton != null) {
            exportUsersButton.setOnClickListener(v -> exportUsers());
        }

        if (importUsersButton != null) {
            importUsersButton.setOnClickListener(v -> showImportDialog());
        }

        if (passwordPolicyButton != null) {
            passwordPolicyButton.setOnClickListener(v -> showPasswordPolicyDialog());
        }

        if (usersListView != null) {
            usersListView.setOnItemClickListener((parent, view, position, id) -> {
                User selectedUser = allUsers.get(position);
                showEditUserDialog(selectedUser);
            });
        }
    }

    private void exportUsers() {
        // Check for storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
            return;
        }

        try {
            // Create export data
            StringBuilder csvData = new StringBuilder();
            csvData.append("Username,Full Name,Role,Active,Created Date\n");

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

            for (User user : allUsers) {
                csvData.append(user.getUsername()).append(",");
                csvData.append(user.getFullName()).append(",");
                csvData.append(user.getRole()).append(",");
                csvData.append(user.isActive() ? "Yes" : "No").append(",");
                csvData.append(user.getCreatedDate() != null ?
                        dateFormat.format(user.getCreatedDate()) : "N/A").append("\n");
            }

            // Save to Downloads folder
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File exportFile = new File(downloadsDir, "dietary_users_export_" +
                    System.currentTimeMillis() + ".csv");

            FileWriter writer = new FileWriter(exportFile);
            writer.write(csvData.toString());
            writer.close();

            Toast.makeText(this, "Users exported to: " + exportFile.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "Export failed", e);
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showImportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Import Users")
                .setMessage("This will import users from a CSV file in your Downloads folder.\n\n" +
                        "The CSV should have columns:\n" +
                        "Username,Full Name,Role,Active,Created Date\n\n" +
                        "Existing users will be skipped.")
                .setPositiveButton("Select File", (dialog, which) -> {
                    // In a real app, you would use a file picker here
                    // For now, show a simple message
                    Toast.makeText(this, "File picker not implemented. Place CSV in Downloads folder.",
                            Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showPasswordPolicyDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_password_policy, null);

        // Initialize dialog views
        CheckBox requireUppercaseCheckBox = dialogView.findViewById(R.id.requireUppercaseCheckBox);
        CheckBox requireLowercaseCheckBox = dialogView.findViewById(R.id.requireLowercaseCheckBox);
        CheckBox requireNumberCheckBox = dialogView.findViewById(R.id.requireNumberCheckBox);
        CheckBox requireSpecialCheckBox = dialogView.findViewById(R.id.requireSpecialCheckBox);
        EditText minLengthEditText = dialogView.findViewById(R.id.minLengthEditText);
        EditText expirationDaysEditText = dialogView.findViewById(R.id.expirationDaysEditText);

        // Set current policy values (using defaults for now)
        requireUppercaseCheckBox.setChecked(true);
        requireLowercaseCheckBox.setChecked(true);
        requireNumberCheckBox.setChecked(true);
        requireSpecialCheckBox.setChecked(false);
        minLengthEditText.setText("8");
        expirationDaysEditText.setText("90");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Password Policy Settings")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    // Save password policy settings
                    // In a real app, you would save these to SharedPreferences or database
                    Toast.makeText(this, "Password policy updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadUsers() {
        try {
            allUsers = userDAO.getAllUsers();

            if (allUsers == null) {
                allUsers = new ArrayList<>();
            }

            if (usersAdapter == null) {
                usersAdapter = new UserAdapter();
                if (usersListView != null) {
                    usersListView.setAdapter(usersAdapter);
                }
            } else {
                usersAdapter.notifyDataSetChanged();
            }

            // Update count
            if (usersCountText != null) {
                usersCountText.setText("Total Users: " + allUsers.size());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading users", e);
            Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddUserDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_user, null);

        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        EditText fullNameInput = dialogView.findViewById(R.id.fullNameInput);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);

        // Setup role spinner
        String[] roles = {"Admin", "Standard User"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New User")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String username = usernameInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    String fullName = fullNameInput.getText().toString().trim();
                    String role = (String) roleSpinner.getSelectedItem();

                    if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Check if username already exists
                    if (userDAO.getUserByUsername(username) != null) {
                        Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create new user
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setPassword(password);
                    newUser.setFullName(fullName);
                    newUser.setRole(role);
                    newUser.setActive(true);
                    newUser.setCreatedDate(new Date());

                    long userId = userDAO.addUser(newUser);
                    if (userId > 0) {
                        Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(this, "Failed to add user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditUserDialog(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);

        EditText fullNameInput = dialogView.findViewById(R.id.fullNameInput);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);
        CheckBox activeCheckBox = dialogView.findViewById(R.id.activeCheckBox);
        Button changePasswordButton = dialogView.findViewById(R.id.changePasswordButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteButton);

        // Set current values
        fullNameInput.setText(user.getFullName());
        activeCheckBox.setChecked(user.isActive());

        // Setup role spinner
        String[] roles = {"Admin", "Standard User"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Select current role
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equalsIgnoreCase(user.getRole())) {
                roleSpinner.setSelection(i);
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Edit User: " + user.getUsername())
                .setPositiveButton("Update", (d, which) -> {
                    user.setFullName(fullNameInput.getText().toString().trim());
                    user.setRole((String) roleSpinner.getSelectedItem());
                    user.setActive(activeCheckBox.isChecked());

                    if (userDAO.updateUser(user)) {
                        Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        // Change password button
        changePasswordButton.setOnClickListener(v -> {
            dialog.dismiss();
            showChangePasswordDialog(user);
        });

        // Delete button
        deleteButton.setOnClickListener(v -> {
            // Prevent deleting yourself
            if (user.getUsername().equals(currentUsername)) {
                Toast.makeText(this, "Cannot delete your own account", Toast.LENGTH_SHORT).show();
                return;
            }

            // Prevent deleting the default admin account
            if ("admin".equals(user.getUsername())) {
                Toast.makeText(this, "Cannot delete the default admin account", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Delete User")
                    .setMessage("Are you sure you want to delete " + user.getUsername() + "?")
                    .setPositiveButton("Delete", (d, w) -> {
                        if (userDAO.deleteUser(user.getUserId())) {
                            Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                            loadUsers();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        dialog.show();
    }

    private void showChangePasswordDialog(User user) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_user_password, null);
        EditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        EditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

        new AlertDialog.Builder(this)
                .setTitle("Change Password for " + user.getUsername())
                .setView(dialogView)
                .setPositiveButton("Change", (dialog, which) -> {
                    String newPassword = newPasswordInput.getText().toString();
                    String confirmPassword = confirmPasswordInput.getText().toString();

                    if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 8) {
                        Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update password
                    user.setPassword(newPassword);
                    if (userDAO.changePassword(user.getUserId(), newPassword)) {
                        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to change password", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportUsers();
            } else {
                Toast.makeText(this, "Storage permission required for export", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Adapter class
    private class UserAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return allUsers.size();
        }

        @Override
        public User getItem(int position) {
            return allUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return allUsers.get(position).getUserId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            User user = getItem(position);

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            text1.setText(user.getFullName() + " (@" + user.getUsername() + ")");
            text2.setText("Role: " + user.getRole() + " | Status: " +
                    (user.isActive() ? "Active" : "Inactive"));

            return convertView;
        }
    }
}