package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

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

    // Data
    private List<User> allUsers = new ArrayList<>();
    private ArrayAdapter<User> usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        // Get user data from intent
        currentUsername = getIntent().getStringExtra("current_user");
        currentUserRole = getIntent().getStringExtra("user_role");
        currentUserFullName = getIntent().getStringExtra("user_full_name");

        // FIXED: Check admin access for both "Admin" and "Administrator" roles (case-insensitive)
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
    }

    private void setupListeners() {
        addUserButton.setOnClickListener(v -> showAddUserDialog());
        refreshButton.setOnClickListener(v -> loadUsers());

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // User list item click
        usersListView.setOnItemClickListener((parent, view, position, id) -> {
            User selectedUser = allUsers.get(position);
            showUserOptionsDialog(selectedUser);
        });
    }

    private void loadUsers() {
        try {
            allUsers = userDAO.getAllUsers();

            // Create adapter
            usersAdapter = new ArrayAdapter<User>(this, android.R.layout.simple_list_item_2, android.R.id.text1, allUsers) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    User user = allUsers.get(position);
                    TextView text1 = view.findViewById(android.R.id.text1);
                    TextView text2 = view.findViewById(android.R.id.text2);

                    text1.setText(user.getFullName() + " (" + user.getUsername() + ")");
                    text2.setText("Role: " + user.getRole() + " | Status: " + (user.isActive() ? "Active" : "Inactive"));

                    return view;
                }
            };

            usersListView.setAdapter(usersAdapter);

            // Update count
            if (usersCountText != null) {
                usersCountText.setText("Total Users: " + allUsers.size());
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddUserDialog() {
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_form, null);

        // Get references to dialog components - FIXED: Use correct IDs from layout
        EditText usernameEdit = dialogView.findViewById(R.id.usernameInput);
        EditText passwordEdit = dialogView.findViewById(R.id.passwordInput);
        EditText fullNameEdit = dialogView.findViewById(R.id.fullNameInput);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);
        CheckBox activeCheckBox = dialogView.findViewById(R.id.activeCheckBox);

        // Setup role spinner
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"User", "Admin"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New User")
                .setView(dialogView)
                .setPositiveButton("Add", null) // Set to null to override later
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(v -> {
                String username = usernameEdit.getText().toString().trim();
                String password = passwordEdit.getText().toString().trim();
                String fullName = fullNameEdit.getText().toString().trim();
                String role = roleSpinner.getSelectedItem().toString();
                boolean isActive = activeCheckBox.isChecked();

                if (validateUserInput(username, password, fullName)) {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setPassword(password);
                    newUser.setFullName(fullName);
                    newUser.setRole(role);
                    newUser.setActive(isActive);

                    // FIXED: addUser returns long, check if > 0
                    long result = userDAO.addUser(newUser);
                    if (result > 0) {
                        Toast.makeText(this, "User added successfully!", Toast.LENGTH_SHORT).show();
                        loadUsers();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Error adding user. Username may already exist.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

        dialog.show();
    }

    private boolean validateUserInput(String username, String password, String fullName) {
        if (username.isEmpty()) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fullName.isEmpty()) {
            Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void showUserOptionsDialog(User user) {
        String[] options = {"Edit User", "Reset Password", "Delete User"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(user.getFullName() + " (" + user.getUsername() + ")")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showEditUserDialog(user);
                            break;
                        case 1:
                            showResetPasswordDialog(user);
                            break;
                        case 2:
                            showDeleteUserConfirmation(user);
                            break;
                    }
                })
                .show();
    }

    private void showEditUserDialog(User user) {
        // Inflate custom dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_form, null);

        // Get references to dialog components - FIXED: Use correct IDs
        EditText usernameEdit = dialogView.findViewById(R.id.usernameInput);
        EditText passwordEdit = dialogView.findViewById(R.id.passwordInput);
        EditText fullNameEdit = dialogView.findViewById(R.id.fullNameInput);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);
        CheckBox activeCheckBox = dialogView.findViewById(R.id.activeCheckBox);

        // Pre-populate with existing data
        usernameEdit.setText(user.getUsername());
        usernameEdit.setEnabled(false); // Username cannot be changed
        passwordEdit.setVisibility(View.GONE); // Don't show password field in edit
        fullNameEdit.setText(user.getFullName());
        activeCheckBox.setChecked(user.isActive());

        // Setup role spinner
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"User", "Admin"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Set current role
        roleSpinner.setSelection(user.getRole().equals("Admin") ? 1 : 0);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit User")
                .setView(dialogView)
                .setPositiveButton("Update", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button updateButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            updateButton.setOnClickListener(v -> {
                String fullName = fullNameEdit.getText().toString().trim();
                String role = roleSpinner.getSelectedItem().toString();
                boolean isActive = activeCheckBox.isChecked();

                if (fullName.isEmpty()) {
                    Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                user.setFullName(fullName);
                user.setRole(role);
                user.setActive(isActive);

                // FIXED: updateUser returns boolean
                if (userDAO.updateUser(user)) {
                    Toast.makeText(this, "User updated successfully!", Toast.LENGTH_SHORT).show();
                    loadUsers();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Error updating user", Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void showResetPasswordDialog(User user) {
        EditText passwordEdit = new EditText(this);
        passwordEdit.setHint("Enter new password");
        passwordEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Reset Password for " + user.getUsername())
                .setView(passwordEdit)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String newPassword = passwordEdit.getText().toString().trim();
                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // FIXED: Use changePassword method with userId
                    if (userDAO.changePassword(user.getUserId(), newPassword)) {
                        Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error resetting password", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteUserConfirmation(User user) {
        // Prevent deletion of current user
        if (user.getUsername().equals(currentUsername)) {
            Toast.makeText(this, "Cannot delete your own account", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user '" + user.getUsername() + "'?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // FIXED: Use deleteUser method with userId
                    if (userDAO.deleteUser(user.getUserId())) {
                        Toast.makeText(this, "User deleted successfully!", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(this, "Error deleting user", Toast.LENGTH_LONG).show();
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}