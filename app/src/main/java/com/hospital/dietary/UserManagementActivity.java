package com.hospital.dietary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.hospital.dietary.dao.UserDAO;
import com.hospital.dietary.models.User;
import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private static final String TAG = "UserManagementActivity";

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
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);
        userDAO = new UserDAO(dbHelper);

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Management");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initializeUI();
        setupListeners();
        loadUsers();
    }

    private void initializeUI() {
        usersListView = findViewById(R.id.usersListView);
        usersCountText = findViewById(R.id.usersCountText);
        addUserButton = findViewById(R.id.addUserButton);
        refreshButton = findViewById(R.id.refreshButton);
        backButton = findViewById(R.id.backButton);
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

        if (usersListView != null) {
            usersListView.setOnItemClickListener((parent, view, position, id) -> {
                User selectedUser = allUsers.get(position);
                showEditUserDialog(selectedUser);
            });
        }
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
            Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null);

        EditText usernameInput = dialogView.findViewById(R.id.usernameInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        EditText fullNameInput = dialogView.findViewById(R.id.fullNameInput);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);
        CheckBox activeCheckBox = dialogView.findViewById(R.id.activeCheckBox);

        // Setup role spinner
        String[] roles = {"User", "Staff", "Admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        activeCheckBox.setChecked(true);

        builder.setView(dialogView)
                .setTitle("Add New User")
                .setPositiveButton("Add", (dialog, which) -> {
                    String username = usernameInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    String fullName = fullNameInput.getText().toString().trim();
                    String role = (String) roleSpinner.getSelectedItem();
                    boolean isActive = activeCheckBox.isChecked();

                    if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                        Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User newUser = new User(username, password, fullName, role);
                    newUser.setActive(isActive);

                    long result = userDAO.insertUser(newUser);
                    if (result > 0) {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);

        TextView usernameText = dialogView.findViewById(R.id.usernameText);
        EditText fullNameInput = dialogView.findViewById(R.id.fullNameInput);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);
        CheckBox activeCheckBox = dialogView.findViewById(R.id.activeCheckBox);
        Button changePasswordButton = dialogView.findViewById(R.id.changePasswordButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteButton);

        usernameText.setText("Username: " + user.getUsername());
        fullNameInput.setText(user.getFullName());
        activeCheckBox.setChecked(user.isActive());

        // Setup role spinner
        String[] roles = {"User", "Staff", "Admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        // Set current role
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equalsIgnoreCase(user.getRole())) {
                roleSpinner.setSelection(i);
                break;
            }
        }

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("Edit User")
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
                    .setPositiveButton("Delete", (d2, w) -> {
                        if (userDAO.deleteUser(user.getUserId())) {
                            Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadUsers();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);

        EditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        EditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

        builder.setView(dialogView)
                .setTitle("Change Password for " + user.getUsername())
                .setPositiveButton("Change", (dialog, which) -> {
                    String newPassword = newPasswordInput.getText().toString();
                    String confirmPassword = confirmPasswordInput.getText().toString();

                    if (newPassword.isEmpty()) {
                        Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    user.setPassword(newPassword);
                    if (userDAO.updateUser(user)) {
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

    // Custom adapter for users list
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
                convertView = LayoutInflater.from(UserManagementActivity.this)
                        .inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            User user = getItem(position);

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            text1.setText(user.getFullName() + " (" + user.getUsername() + ")");
            text2.setText("Role: " + user.getRole() + " | Status: " +
                    (user.isActive() ? "Active" : "Inactive"));

            // Highlight admin users
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                text1.setTextColor(android.graphics.Color.parseColor("#e74c3c"));
            } else {
                text1.setTextColor(android.graphics.Color.BLACK);
            }

            return convertView;
        }
    }
}