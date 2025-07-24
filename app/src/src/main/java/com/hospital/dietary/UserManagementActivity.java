package com.hospital.dietary;

import android.app.AlertDialog;
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

        // Check admin access for both "Admin" and "Administrator" roles (case-insensitive)
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

        Log.d(TAG, "usersListView: " + (usersListView != null ? "Found" : "NULL"));
        Log.d(TAG, "usersCountText: " + (usersCountText != null ? "Found" : "NULL"));
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

        // User list item click - FIXED with proper bounds checking
        if (usersListView != null) {
            usersListView.setOnItemClickListener((parent, view, position, id) -> {
                Log.d(TAG, "ListView item clicked at position: " + position + ", total users: " + allUsers.size());

                if (position >= 0 && position < allUsers.size()) {
                    User selectedUser = allUsers.get(position);
                    Log.d(TAG, "Selected user: " + selectedUser.getUsername());
                    showUserOptionsDialog(selectedUser);
                } else {
                    Log.w(TAG, "Invalid position clicked: " + position);
                    Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadUsers() {
        try {
            Log.d(TAG, "Loading users...");
            allUsers = userDAO.getAllUsers();
            Log.d(TAG, "Loaded " + allUsers.size() + " users from database");

            // Create custom adapter for Samsung compatibility
            usersAdapter = new UserAdapter();
            if (usersListView != null) {
                usersListView.setAdapter(usersAdapter);
            }

            // Update count
            if (usersCountText != null) {
                usersCountText.setText("Total Users: " + allUsers.size());
            }

            // Log users for debugging
            for (int i = 0; i < allUsers.size(); i++) {
                User user = allUsers.get(i);
                Log.d(TAG, "User " + i + ": " + user.getUsername() + " - " + user.getFullName());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading users: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading users: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Custom UserAdapter for Samsung One UI compatibility
    private class UserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return allUsers.size();
        }

        @Override
        public Object getItem(int position) {
            if (position >= 0 && position < allUsers.size()) {
                return allUsers.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (position >= 0 && position < allUsers.size()) {
                return allUsers.get(position).getUserId();
            }
            return -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ViewHolder holder;

            if (view == null) {
                view = LayoutInflater.from(UserManagementActivity.this)
                        .inflate(R.layout.item_user_management, parent, false);
                holder = new ViewHolder();
                holder.fullNameText = view.findViewById(R.id.fullNameText);
                holder.usernameText = view.findViewById(R.id.usernameText);
                holder.roleText = view.findViewById(R.id.roleText);
                holder.statusText = view.findViewById(R.id.statusText);
                holder.createdDateText = view.findViewById(R.id.createdDateText);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            if (position >= 0 && position < allUsers.size()) {
                User user = allUsers.get(position);

                // Set explicit colors for Samsung compatibility
                if (holder.fullNameText != null) {
                    holder.fullNameText.setText(user.getFullName());
                    holder.fullNameText.setTextColor(0xFF2c3e50); // Dark gray
                }

                if (holder.usernameText != null) {
                    holder.usernameText.setText("@" + user.getUsername());
                    holder.usernameText.setTextColor(0xFF7f8c8d); // Medium gray
                }

                if (holder.roleText != null) {
                    // Role with icon
                    String roleIcon = "Admin".equalsIgnoreCase(user.getRole()) ? "👑" : "👤";
                    holder.roleText.setText(roleIcon + " " + user.getRole());
                    holder.roleText.setTextColor(0xFF374151); // Dark gray
                }

                if (holder.statusText != null) {
                    // Status with color coding
                    if (user.isActive()) {
                        holder.statusText.setText("✅ Active");
                        holder.statusText.setTextColor(0xFF27ae60); // Green
                    } else {
                        holder.statusText.setText("❌ Inactive");
                        holder.statusText.setTextColor(0xFFe74c3c); // Red
                    }
                }

                if (holder.createdDateText != null) {
                    // Created date
                    holder.createdDateText.setText("📅 Created: " + user.getCreatedDate());
                    holder.createdDateText.setTextColor(0xFF9ca3af); // Light gray
                }

                // Set background for better visibility
                view.setBackgroundColor(0xFFFFFFFF); // White background
            }

            return view;
        }

        private class ViewHolder {
            TextView fullNameText;
            TextView usernameText;
            TextView roleText;
            TextView statusText;
            TextView createdDateText;
        }
    }

    private void showAddUserDialog() {
        // Create EditTexts programmatically to avoid layout issues
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Username
        TextView usernameLabel = new TextView(this);
        usernameLabel.setText("Username *");
        usernameLabel.setTextColor(0xFF2c3e50);
        usernameLabel.setTextSize(14);
        layout.addView(usernameLabel);

        EditText usernameEdit = new EditText(this);
        usernameEdit.setHint("Enter username");
        usernameEdit.setTextColor(0xFF2c3e50);
        layout.addView(usernameEdit);

        // Password
        TextView passwordLabel = new TextView(this);
        passwordLabel.setText("Password *");
        passwordLabel.setTextColor(0xFF2c3e50);
        passwordLabel.setTextSize(14);
        layout.addView(passwordLabel);

        EditText passwordEdit = new EditText(this);
        passwordEdit.setHint("Enter password (min 6 characters)");
        passwordEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEdit.setTextColor(0xFF2c3e50);
        layout.addView(passwordEdit);

        // Full Name
        TextView fullNameLabel = new TextView(this);
        fullNameLabel.setText("Full Name *");
        fullNameLabel.setTextColor(0xFF2c3e50);
        fullNameLabel.setTextSize(14);
        layout.addView(fullNameLabel);

        EditText fullNameEdit = new EditText(this);
        fullNameEdit.setHint("Enter full name");
        fullNameEdit.setTextColor(0xFF2c3e50);
        layout.addView(fullNameEdit);

        // Role Spinner
        TextView roleLabel = new TextView(this);
        roleLabel.setText("Role *");
        roleLabel.setTextColor(0xFF2c3e50);
        roleLabel.setTextSize(14);
        layout.addView(roleLabel);

        Spinner roleSpinner = new Spinner(this);
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"User", "Admin"});
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);
        layout.addView(roleSpinner);

        // Active Checkbox
        CheckBox activeCheckBox = new CheckBox(this);
        activeCheckBox.setText("Account is active");
        activeCheckBox.setTextColor(0xFF2c3e50);
        activeCheckBox.setChecked(true);
        layout.addView(activeCheckBox);

        // Create dialog with enhanced Samsung compatibility
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Add New User")
                .setView(layout)
                .setPositiveButton("Add", null) // Set to null to override later
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

            // Ensure button colors are explicit
            addButton.setTextColor(0xFF2196F3);
            cancelButton.setTextColor(0xFF7f8c8d);

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

                    long result = userDAO.addUser(newUser);
                    if (result > 0) {
                        Toast.makeText(this, "User added successfully!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadUsers(); // Refresh the list
                    } else {
                        Toast.makeText(this, "Error adding user", Toast.LENGTH_LONG).show();
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

        // Check if username already exists
        if (userDAO.getUserByUsername(username) != null) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showUserOptionsDialog(User user) {
        Log.d(TAG, "Showing options dialog for user: " + user.getUsername());

        String[] options = {"Edit User", "Reset Password", "Delete User"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle(user.getFullName() + " (" + user.getUsername() + ")")
                .setItems(options, (dialog, which) -> {
                    Log.d(TAG, "Option selected: " + which);
                    switch (which) {
                        case 0: // Edit User
                            showEditUserDialog(user);
                            break;
                        case 1: // Reset Password
                            showResetPasswordDialog(user);
                            break;
                        case 2: // Delete User
                            showDeleteUserConfirmation(user);
                            break;
                    }
                })
                .show();
    }

    private void showEditUserDialog(User user) {
        Toast.makeText(this, "Edit user functionality - to be implemented", Toast.LENGTH_SHORT).show();
    }

    private void showResetPasswordDialog(User user) {
        // Create simple programmatic layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // New Password
        TextView newPasswordLabel = new TextView(this);
        newPasswordLabel.setText("New Password *");
        newPasswordLabel.setTextColor(0xFF2c3e50);
        layout.addView(newPasswordLabel);

        EditText newPasswordEdit = new EditText(this);
        newPasswordEdit.setHint("Enter new password (min 6 characters)");
        newPasswordEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswordEdit.setTextColor(0xFF2c3e50);
        layout.addView(newPasswordEdit);

        // Confirm Password
        TextView confirmPasswordLabel = new TextView(this);
        confirmPasswordLabel.setText("Confirm Password *");
        confirmPasswordLabel.setTextColor(0xFF2c3e50);
        layout.addView(confirmPasswordLabel);

        EditText confirmPasswordEdit = new EditText(this);
        confirmPasswordEdit.setHint("Confirm new password");
        confirmPasswordEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordEdit.setTextColor(0xFF2c3e50);
        layout.addView(confirmPasswordEdit);

        // Force Change Checkbox
        CheckBox forceChangeCheckBox = new CheckBox(this);
        forceChangeCheckBox.setText("Force password change on next login");
        forceChangeCheckBox.setTextColor(0xFF2c3e50);
        layout.addView(forceChangeCheckBox);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Reset Password for " + user.getUsername())
                .setView(layout)
                .setPositiveButton("Reset Password", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button resetButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            resetButton.setTextColor(0xFF2196F3);

            resetButton.setOnClickListener(v -> {
                String newPassword = newPasswordEdit.getText().toString();
                String confirmPassword = confirmPasswordEdit.getText().toString();

                if (newPassword.length() < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userDAO.changePassword(user.getUserId(), newPassword)) {
                    if (forceChangeCheckBox.isChecked()) {
                        user.setMustChangePassword(true);
                        userDAO.updateUser(user);
                    }
                    Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Error resetting password", Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private void showDeleteUserConfirmation(User user) {
        new AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete user '" + user.getUsername() + "'?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
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