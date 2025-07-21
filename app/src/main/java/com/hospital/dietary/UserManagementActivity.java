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

        // Check admin access
        if (!"Admin".equalsIgnoreCase(currentUserRole)) {
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
            usersCountText.setText("Total Users: " + allUsers.size());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New User");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText usernameInput = new EditText(this);
        usernameInput.setHint("Username");
        layout.addView(usernameInput);

        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password (min 6 characters)");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);

        final EditText fullNameInput = new EditText(this);
        fullNameInput.setHint("Full Name");
        layout.addView(fullNameInput);

        final Spinner roleSpinner = new Spinner(this);
        String[] roles = {"User", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        layout.addView(roleSpinner);

        final CheckBox activeCheckBox = new CheckBox(this);
        activeCheckBox.setText("User Active");
        activeCheckBox.setChecked(true);
        layout.addView(activeCheckBox);

        builder.setView(layout);

        builder.setPositiveButton("Add User", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String fullName = fullNameInput.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();
            boolean isActive = activeCheckBox.isChecked();

            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            User newUser = new User(username, password, fullName, role);
            newUser.setActive(isActive);

            long result = userDAO.addUser(newUser);
            if (result > 0) {
                Toast.makeText(this, "User added successfully!", Toast.LENGTH_SHORT).show();
                loadUsers();
            } else {
                Toast.makeText(this, "Error adding user. Username may already exist.", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showUserOptionsDialog(User user) {
        String[] options = {"Edit User", "Reset Password", "Toggle Active Status", "Delete User"};

        new AlertDialog.Builder(this)
                .setTitle("User: " + user.getFullName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showEditUserDialog(user);
                            break;
                        case 1:
                            showResetPasswordDialog(user);
                            break;
                        case 2:
                            toggleUserStatus(user);
                            break;
                        case 3:
                            showDeleteUserConfirmation(user);
                            break;
                    }
                })
                .show();
    }

    private void showEditUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit User: " + user.getUsername());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText fullNameInput = new EditText(this);
        fullNameInput.setHint("Full Name");
        fullNameInput.setText(user.getFullName());
        layout.addView(fullNameInput);

        final Spinner roleSpinner = new Spinner(this);
        String[] roles = {"User", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        roleSpinner.setSelection(user.getRole().equals("Admin") ? 1 : 0);
        layout.addView(roleSpinner);

        final CheckBox activeCheckBox = new CheckBox(this);
        activeCheckBox.setText("User Active");
        activeCheckBox.setChecked(user.isActive());
        layout.addView(activeCheckBox);

        builder.setView(layout);

        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String fullName = fullNameInput.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();
            boolean isActive = activeCheckBox.isChecked();

            if (fullName.isEmpty()) {
                Toast.makeText(this, "Full name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setFullName(fullName);
            user.setRole(role);
            user.setActive(isActive);

            boolean success = userDAO.updateUser(user);
            if (success) {
                Toast.makeText(this, "User updated successfully!", Toast.LENGTH_SHORT).show();
                loadUsers();
            } else {
                Toast.makeText(this, "Error updating user", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showResetPasswordDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password: " + user.getUsername());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        final EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("New Password (min 6 characters)");
        newPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(newPasswordInput);

        final EditText confirmPasswordInput = new EditText(this);
        confirmPasswordInput.setHint("Confirm New Password");
        confirmPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(confirmPasswordInput);

        builder.setView(layout);

        builder.setPositiveButton("Reset Password", (dialog, which) -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in both password fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = userDAO.changePassword(user.getUserId(), newPassword);
            if (success) {
                Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error resetting password", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void toggleUserStatus(User user) {
        String action = user.isActive() ? "deactivate" : "activate";

        new AlertDialog.Builder(this)
                .setTitle("Confirm Action")
                .setMessage("Are you sure you want to " + action + " user '" + user.getUsername() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    user.setActive(!user.isActive());
                    boolean success = userDAO.updateUser(user);

                    if (success) {
                        Toast.makeText(this, "User " + (user.isActive() ? "activated" : "deactivated") + " successfully!", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(this, "Error updating user status", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showDeleteUserConfirmation(User user) {
        if (user.getUsername().equals(currentUsername)) {
            Toast.makeText(this, "You cannot delete your own account", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user '" + user.getUsername() + "'?\n\nThis action cannot be undone!")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = userDAO.deleteUser(user.getUserId());

                    if (success) {
                        Toast.makeText(this, "User deleted successfully!", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    } else {
                        Toast.makeText(this, "Error deleting user", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
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