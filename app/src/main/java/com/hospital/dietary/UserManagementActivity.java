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
        if (addUserButton != null) {
            addUserButton.setOnClickListener(v -> showAddUserDialog());
        }

        if (refreshButton != null) {
            refreshButton.setOnClickListener(v -> loadUsers());
        }

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Fixed ListView click listener for Samsung devices
        if (usersListView != null) {
            // Set ListView properties for better click handling
            usersListView.setItemsCanFocus(false);
            usersListView.setFocusable(true);
            usersListView.setFocusableInTouchMode(true);
            usersListView.setClickable(true);

            usersListView.setOnItemClickListener((parent, view, position, id) -> {
                Log.d(TAG, "ListView item clicked at position: " + position);

                if (position >= 0 && position < allUsers.size()) {
                    User selectedUser = allUsers.get(position);
                    Log.d(TAG, "Selected user: " + selectedUser.getUsername());
                    showUserManagementDialog(selectedUser);
                }
            });
        }
    }

    private void loadUsers() {
        try {
            allUsers = userDAO.getAllUsers();
            Log.d(TAG, "Loaded " + allUsers.size() + " users from database");

            // Create custom adapter
            usersAdapter = new UserAdapter();
            if (usersListView != null) {
                usersListView.setAdapter(usersAdapter);
            }

            // Update count
            if (usersCountText != null) {
                usersCountText.setText("Total Users: " + allUsers.size());
            }

        } catch (Exception e) {
            Log.e(TAG, "Error loading users: " + e.getMessage());
            Toast.makeText(this, "Error loading users", Toast.LENGTH_LONG).show();
        }
    }

    private void showUserManagementDialog(final User user) {
        // Create the layout programmatically
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        // Username (read-only)
        TextView usernameLabel = new TextView(this);
        usernameLabel.setText("Username:");
        usernameLabel.setTextSize(14);
        usernameLabel.setTextColor(0xFF7f8c8d);
        layout.addView(usernameLabel);

        TextView usernameText = new TextView(this);
        usernameText.setText(user.getUsername());
        usernameText.setTextSize(18);
        usernameText.setTextColor(0xFF2c3e50);
        usernameText.setPadding(0, 5, 0, 20);
        usernameText.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(usernameText);

        // Full Name (read-only)
        TextView fullNameLabel = new TextView(this);
        fullNameLabel.setText("Full Name:");
        fullNameLabel.setTextSize(14);
        fullNameLabel.setTextColor(0xFF7f8c8d);
        layout.addView(fullNameLabel);

        TextView fullNameText = new TextView(this);
        fullNameText.setText(user.getFullName());
        fullNameText.setTextSize(18);
        fullNameText.setTextColor(0xFF2c3e50);
        fullNameText.setPadding(0, 5, 0, 30);
        layout.addView(fullNameText);

        // Role selection
        TextView roleLabel = new TextView(this);
        roleLabel.setText("Role:");
        roleLabel.setTextSize(14);
        roleLabel.setTextColor(0xFF7f8c8d);
        layout.addView(roleLabel);

        final Spinner roleSpinner = new Spinner(this);
        String[] roles = {"Admin", "User", "Viewer"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, roles);
        roleSpinner.setAdapter(roleAdapter);

        // Set current role
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(user.getRole())) {
                roleSpinner.setSelection(i);
                break;
            }
        }
        roleSpinner.setPadding(0, 10, 0, 30);
        layout.addView(roleSpinner);

        // Active status
        final CheckBox activeCheckBox = new CheckBox(this);
        activeCheckBox.setText("Account is active");
        activeCheckBox.setTextColor(0xFF2c3e50);
        activeCheckBox.setTextSize(16);
        activeCheckBox.setChecked(user.isActive());
        activeCheckBox.setPadding(0, 0, 0, 30);
        layout.addView(activeCheckBox);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manage User: " + user.getUsername());
        builder.setView(layout);

        // Add buttons
        builder.setPositiveButton("Save Changes", null);
        builder.setNeutralButton("Change Password", null);
        builder.setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button passwordButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                Button cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                // Style the buttons
                saveButton.setTextColor(0xFF27ae60);
                passwordButton.setTextColor(0xFF3498db);
                cancelButton.setTextColor(0xFF7f8c8d);

                // Save button action
                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newRole = roleSpinner.getSelectedItem().toString();
                        boolean newActive = activeCheckBox.isChecked();

                        // Update user
                        user.setRole(newRole);
                        user.setActive(newActive);

                        if (userDAO.updateUser(user)) {
                            Toast.makeText(UserManagementActivity.this,
                                    "User updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadUsers(); // Refresh list
                        } else {
                            Toast.makeText(UserManagementActivity.this,
                                    "Failed to update user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Change password button action
                passwordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        showChangePasswordDialog(user);
                    }
                });
            }
        });

        dialog.show();

        // Add delete button after showing dialog
        LinearLayout buttonPanel = (LinearLayout) dialog.findViewById(android.R.id.button1).getParent();
        if (buttonPanel != null) {
            Button deleteButton = new Button(this);
            deleteButton.setText("Delete User");
            deleteButton.setTextColor(0xFFFFFFFF);
            deleteButton.setBackgroundColor(0xFFe74c3c);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Prevent deleting yourself
                    if (user.getUsername().equals(currentUsername)) {
                        Toast.makeText(UserManagementActivity.this,
                                "Cannot delete your own account", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new AlertDialog.Builder(UserManagementActivity.this)
                            .setTitle("Delete User")
                            .setMessage("Are you sure you want to delete " + user.getUsername() + "?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int w) {
                                    if (userDAO.deleteUser(user.getUserId())) {
                                        Toast.makeText(UserManagementActivity.this,
                                                "User deleted successfully", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        loadUsers();
                                    } else {
                                        Toast.makeText(UserManagementActivity.this,
                                                "Failed to delete user", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(20, 0, 0, 0);
            deleteButton.setLayoutParams(params);
            buttonPanel.addView(deleteButton, 0);
        }
    }

    private void showChangePasswordDialog(final User user) {
        // Create layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        // New password field
        TextView newPasswordLabel = new TextView(this);
        newPasswordLabel.setText("New Password:");
        newPasswordLabel.setTextSize(14);
        newPasswordLabel.setTextColor(0xFF7f8c8d);
        layout.addView(newPasswordLabel);

        final EditText newPasswordEdit = new EditText(this);
        newPasswordEdit.setHint("Enter new password (min 6 characters)");
        newPasswordEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        newPasswordEdit.setPadding(0, 10, 0, 20);
        layout.addView(newPasswordEdit);

        // Confirm password field
        TextView confirmPasswordLabel = new TextView(this);
        confirmPasswordLabel.setText("Confirm Password:");
        confirmPasswordLabel.setTextSize(14);
        confirmPasswordLabel.setTextColor(0xFF7f8c8d);
        layout.addView(confirmPasswordLabel);

        final EditText confirmPasswordEdit = new EditText(this);
        confirmPasswordEdit.setHint("Re-enter new password");
        confirmPasswordEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        confirmPasswordEdit.setPadding(0, 10, 0, 20);
        layout.addView(confirmPasswordEdit);

        // Show password checkbox
        final CheckBox showPasswordCheckBox = new CheckBox(this);
        showPasswordCheckBox.setText("Show passwords");
        showPasswordCheckBox.setTextColor(0xFF2c3e50);
        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int inputType = isChecked ?
                        android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD :
                        android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
                newPasswordEdit.setInputType(inputType);
                confirmPasswordEdit.setInputType(inputType);
                // Maintain cursor position
                newPasswordEdit.setSelection(newPasswordEdit.getText().length());
                confirmPasswordEdit.setSelection(confirmPasswordEdit.getText().length());
            }
        });
        layout.addView(showPasswordCheckBox);

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password for " + user.getUsername());
        builder.setView(layout);
        builder.setPositiveButton("Change Password", null);
        builder.setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface d) {
                Button changeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                changeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newPassword = newPasswordEdit.getText().toString();
                        String confirmPassword = confirmPasswordEdit.getText().toString();

                        if (newPassword.isEmpty()) {
                            newPasswordEdit.setError("Password is required");
                            return;
                        }

                        if (newPassword.length() < 6) {
                            newPasswordEdit.setError("Password must be at least 6 characters");
                            return;
                        }

                        if (!newPassword.equals(confirmPassword)) {
                            confirmPasswordEdit.setError("Passwords do not match");
                            return;
                        }

                        if (userDAO.changePassword(user.getUserId(), newPassword)) {
                            Toast.makeText(UserManagementActivity.this,
                                    "Password changed successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(UserManagementActivity.this,
                                    "Failed to change password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        dialog.show();
    }

    private void showAddUserDialog() {
        // Create a custom layout for the dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);

        // Username input
        TextView usernameLabel = new TextView(this);
        usernameLabel.setText("Username:");
        usernameLabel.setTextSize(14);
        usernameLabel.setTextColor(0xFF7f8c8d);
        layout.addView(usernameLabel);

        final EditText usernameEdit = new EditText(this);
        usernameEdit.setHint("Enter username");
        usernameEdit.setPadding(0, 10, 0, 20);
        layout.addView(usernameEdit);

        // Password input
        TextView passwordLabel = new TextView(this);
        passwordLabel.setText("Password:");
        passwordLabel.setTextSize(14);
        passwordLabel.setTextColor(0xFF7f8c8d);
        layout.addView(passwordLabel);

        final EditText passwordEdit = new EditText(this);
        passwordEdit.setHint("Enter password (min 6 characters)");
        passwordEdit.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEdit.setPadding(0, 10, 0, 20);
        layout.addView(passwordEdit);

        // Full name input
        TextView fullNameLabel = new TextView(this);
        fullNameLabel.setText("Full Name:");
        fullNameLabel.setTextSize(14);
        fullNameLabel.setTextColor(0xFF7f8c8d);
        layout.addView(fullNameLabel);

        final EditText fullNameEdit = new EditText(this);
        fullNameEdit.setHint("Enter full name");
        fullNameEdit.setPadding(0, 10, 0, 20);
        layout.addView(fullNameEdit);

        // Role selection
        TextView roleLabel = new TextView(this);
        roleLabel.setText("Role:");
        roleLabel.setTextSize(14);
        roleLabel.setTextColor(0xFF7f8c8d);
        layout.addView(roleLabel);

        final Spinner roleSpinner = new Spinner(this);
        String[] roles = {"Admin", "User", "Viewer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, roles);
        roleSpinner.setAdapter(adapter);
        roleSpinner.setSelection(1); // Default to "User"
        roleSpinner.setPadding(0, 10, 0, 20);
        layout.addView(roleSpinner);

        // Active checkbox
        final CheckBox activeCheckBox = new CheckBox(this);
        activeCheckBox.setText("Account is active");
        activeCheckBox.setTextColor(0xFF2c3e50);
        activeCheckBox.setChecked(true);
        layout.addView(activeCheckBox);

        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add New User")
                .setView(layout)
                .setPositiveButton("Add", null)
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

    // Custom UserAdapter
    private class UserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return allUsers.size();
        }

        @Override
        public Object getItem(int position) {
            return allUsers.get(position);
        }

        @Override
        public long getItemId(int position) {
            return allUsers.get(position).getUserId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            User user = allUsers.get(position);

            TextView text1 = view.findViewById(android.R.id.text1);
            TextView text2 = view.findViewById(android.R.id.text2);

            if (text1 != null) {
                text1.setText(user.getFullName());
                text1.setTextColor(0xFF2c3e50);
                text1.setTextSize(16);
            }

            if (text2 != null) {
                String details = user.getUsername() + " • " + user.getRole();
                if (!user.isActive()) {
                    details += " • INACTIVE";
                }
                text2.setText(details);
                text2.setTextColor(user.isActive() ? 0xFF7f8c8d : 0xFFe74c3c);
                text2.setTextSize(14);
            }

            // IMPORTANT: Don't make individual items clickable
            view.setClickable(false);
            view.setFocusable(false);

            return view;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}