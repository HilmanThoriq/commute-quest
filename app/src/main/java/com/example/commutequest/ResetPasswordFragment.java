package com.example.commutequest;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.commutequest.R;
import com.example.commutequest.api.APIService;
import com.example.commutequest.api.RetrofitClient;
import com.example.commutequest.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordFragment extends Fragment {

    private static final String TAG = "ResetPasswordFragment";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_CODE = "code";

    // Email and code parameters that will be passed from AuthActivity
    private String email;
    private String code;

    // UI Components
    private EditText currentPasswordInput;
    private EditText newPasswordInput;
    private EditText confirmPasswordInput;
    private CheckBox showLastPasswordCheckbox;
    private CheckBox showConfirmPasswordCheckbox;
    private Button resetButton;
    private ImageView backButton;

    // Session manager to get user info
    private SessionManager sessionManager;
    // API service for network calls
    private APIService apiService;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    public static ResetPasswordFragment newInstance() {
        return new ResetPasswordFragment();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment with the provided email and code.
     *
     * @param email User's email address for password reset
     * @param code Reset verification code
     * @return A new instance of ResetPasswordFragment
     */
    public static ResetPasswordFragment newInstance(String email, String code) {
        ResetPasswordFragment fragment = new ResetPasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_CODE, code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize API service
        apiService = RetrofitClient.getInstance().getApi();
        // Initialize session manager
        sessionManager = new SessionManager(getContext());

        // Get arguments if available
        if (getArguments() != null) {
            email = getArguments().getString(ARG_EMAIL);
            code = getArguments().getString(ARG_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        initViews(view);
        // Set up listeners
        setupListeners();

        // Pre-fill email field if available
        // If you have an email field in your UI, you can pre-populate it here
        // Example: emailInput.setText(email);
    }

    private void initViews(View view) {
        currentPasswordInput = view.findViewById(R.id.current_password_input);
        newPasswordInput = view.findViewById(R.id.new_password_input);
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input);
        showLastPasswordCheckbox = view.findViewById(R.id.show_last_password_checkbox);
        showConfirmPasswordCheckbox = view.findViewById(R.id.show_confirm_password_checkbox);
        resetButton = view.findViewById(R.id.reset_button);
        backButton = view.findViewById(R.id.back_button);
    }

    private void setupListeners() {
        // Back button click listener - navigate back to previous page
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Show/hide last password checkbox listener
        showLastPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            togglePasswordVisibility(currentPasswordInput, isChecked);
        });

        // Show/hide confirm password checkbox listener
        // This will toggle visibility for both new password and confirm password fields
        showConfirmPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            togglePasswordVisibility(newPasswordInput, isChecked);
            togglePasswordVisibility(confirmPasswordInput, isChecked);
        });

        // Reset button click listener
        resetButton.setOnClickListener(v -> validateAndChangePassword());
    }

    private void togglePasswordVisibility(EditText editText, boolean showPassword) {
        if (showPassword) {
            // Show password
            editText.setTransformationMethod(null);
        } else {
            // Hide password
            editText.setTransformationMethod(new PasswordTransformationMethod());
        }
        // Move cursor to end of text
        editText.setSelection(editText.getText().length());
    }

    private void validateAndChangePassword() {
        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate inputs
        if (currentPassword.isEmpty()) {
            currentPasswordInput.setError("Please enter your current password");
            currentPasswordInput.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            newPasswordInput.setError("Please enter new password");
            newPasswordInput.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Please confirm your new password");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Check if new password and confirm password match
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "New password and confirmation do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if new password is different from current password
        if (currentPassword.equals(newPassword)) {
            Toast.makeText(getContext(), "New password must be different from current password", Toast.LENGTH_SHORT).show();
            return;
        }

        // All validations passed, proceed to change password
        changePassword(currentPassword, newPassword);
    }

    private void changePassword(String currentPassword, String newPassword) {
        // Get authorization header from session manager
        String authHeader = sessionManager.getAuthorizationHeader();
        if (authHeader == null) {
            Toast.makeText(getContext(), "You are not logged in. Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create request object according to your API requirements
        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);

        // Show loading indicator
        showLoading(true);

        // Make API call to reset-password endpoint
        Call<ChangePasswordResponse> call = apiService.changePassword(authHeader, request);
        call.enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessResponse(response.body());
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "API call failed", t);
                Toast.makeText(getContext(), "Failed to connect to server. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleSuccessResponse(ChangePasswordResponse response) {
        if (response.isSuccess()) {
            Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
            // Navigate back or to login screen depending on your app flow
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        } else {
            // API returned success:false with a message
            Toast.makeText(getContext(), response.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleErrorResponse(Response<ChangePasswordResponse> response) {
        try {
            if (response.errorBody() != null) {
                // Parse error response based on your API error format
                String errorMessage = response.errorBody().string();
                Log.e(TAG, "Error: " + errorMessage);

                // Based on error code, show appropriate message
                if (response.code() == 401) {
                    Toast.makeText(getContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to change password. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
            Toast.makeText(getContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean isLoading) {
        // Implement loading indicator here (ProgressBar, etc.)
        if (isLoading) {
            resetButton.setEnabled(false);
            resetButton.setText("Processing...");
        } else {
            resetButton.setEnabled(true);
            resetButton.setText("Change Password");
        }
    }

    // Request and Response classes for the API

    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public ChangePasswordRequest(String oldPassword, String newPassword) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    public static class ChangePasswordResponse {
        private boolean success;
        private String message;
        private String status;
        private Object data;

        public boolean isSuccess() {
            return success || (status != null && status.equalsIgnoreCase("success"));
        }

        public String getMessage() {
            return message;
        }

        public String getStatus() {
            return status;
        }

        public Object getData() {
            return data;
        }
    }
}