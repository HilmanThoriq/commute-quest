package com.example.commutequest;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.commutequest.api.RetrofitClient;
import com.example.commutequest.model.ForgotPasswordRequest;
import com.example.commutequest.model.ForgotPasswordResponse;
import com.example.commutequest.model.ResetPasswordRequest;
import com.example.commutequest.model.ResetPasswordResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyFragment extends Fragment {
    private static final String TAG = "VerifyFragment";

    // UI Elements
    private TextView emailLabel;
    private EditText emailInput;
    private TextView verificationLabel;
    private EditText verificationInput;
    private TextView newPasswordLabel;
    private EditText newPasswordInput;
    private TextView confirmPasswordLabel;
    private EditText confirmPasswordInput;
    private CheckBox showPasswordCheckbox;
    private TextView cancelButton;
    private Button sendVerificationButton;
    private TextView resendCodeButton;
    private Button resetButton;

    // Track current state
    private boolean isInVerificationMode = false;

    public VerifyFragment() {
        // Required empty public constructor
    }

    public static VerifyFragment newInstance() {
        return new VerifyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_verify, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI elements
        initializeViews(view);

        // Set initial UI state - show email input layer
        showEmailInputLayer();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews(View view) {
        emailLabel = view.findViewById(R.id.email_label);
        emailInput = view.findViewById(R.id.email_input);
        verificationLabel = view.findViewById(R.id.verification_label);
        verificationInput = view.findViewById(R.id.verification_input);
        newPasswordLabel = view.findViewById(R.id.new_password_label);
        newPasswordInput = view.findViewById(R.id.new_password_input);
        confirmPasswordLabel = view.findViewById(R.id.confirm_password_label);
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input);
        showPasswordCheckbox = view.findViewById(R.id.show_password_checkbox);
        cancelButton = view.findViewById(R.id.cancel_button);
        sendVerificationButton = view.findViewById(R.id.send_verification_button);
        resendCodeButton = view.findViewById(R.id.resend_code_button);
        resetButton = view.findViewById(R.id.reset_button);
    }

    private void setupClickListeners() {
        // Cancel button - return to previous screen
        cancelButton.setOnClickListener(v -> {
            if (isInVerificationMode) {
                // Go back to email input mode
                showEmailInputLayer();
            } else {
                // Close the fragment
                requireActivity().onBackPressed();
            }
        });

        // Reset Password button - send reset code to email and switch to verification mode
        sendVerificationButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (isValidEmail(email)) {
                //Disable button to prevent multiple clicks
                sendVerificationButton.setEnabled(false);

                // Call the API to send verification code
                sendForgotPasswordRequest(email);
            } else {
                Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            }
        });

        // Resend Code button - resend verification code
        resendCodeButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                // Resend verification code using the API
                sendForgotPasswordRequest(email);
            }
        });

        // Show/Hide Password checkbox
        showPasswordCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                newPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                confirmPasswordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                // Hide password
                newPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmPasswordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // Move cursor to the end
            newPasswordInput.setSelection(newPasswordInput.getText().length());
            confirmPasswordInput.setSelection(confirmPasswordInput.getText().length());
        });

        // Reset button - validate inputs and send reset request
        resetButton.setOnClickListener(v -> {
            String code = verificationInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            // Validate verification code
            if (!isValidVerificationCode(code)) {
                Toast.makeText(requireContext(), "Please enter a valid verification code", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password
            if (!isValidPassword(newPassword)) {
                return;
            }

            // Validate password confirmation
            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // All validations passed, proceed with reset password
            verifyCode(code, newPassword);
        });
    }

    private void showEmailInputLayer() {
        isInVerificationMode = false;

        // Show email elements
        emailLabel.setVisibility(View.VISIBLE);
        emailInput.setVisibility(View.VISIBLE);
        sendVerificationButton.setVisibility(View.VISIBLE);

        // Hide verification elements
        verificationLabel.setVisibility(View.GONE);
        verificationInput.setVisibility(View.GONE);
        newPasswordLabel.setVisibility(View.GONE);
        newPasswordInput.setVisibility(View.GONE);
        confirmPasswordLabel.setVisibility(View.GONE);
        confirmPasswordInput.setVisibility(View.GONE);
        showPasswordCheckbox.setVisibility(View.GONE);
        resendCodeButton.setVisibility(View.GONE);
        resetButton.setVisibility(View.GONE);

        // Update cancel button text
        cancelButton.setText("Cancel");
        cancelButton.setVisibility(View.VISIBLE);
    }

    private void showVerificationLayer() {
        isInVerificationMode = true;

        // Hide email elements
        emailLabel.setVisibility(View.GONE);
        emailInput.setVisibility(View.GONE);
        sendVerificationButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        // Show verification elements
        verificationLabel.setVisibility(View.VISIBLE);
        verificationInput.setVisibility(View.VISIBLE);
        newPasswordLabel.setVisibility(View.VISIBLE);
        newPasswordInput.setVisibility(View.VISIBLE);
        confirmPasswordLabel.setVisibility(View.VISIBLE);
        confirmPasswordInput.setVisibility(View.VISIBLE);
        showPasswordCheckbox.setVisibility(View.VISIBLE);
        resendCodeButton.setVisibility(View.VISIBLE);
        resetButton.setVisibility(View.VISIBLE);
    }

    private boolean isValidEmail(String email) {
        // Simple email validation
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidVerificationCode(String code) {
        // Simple validation - check if code is not empty and contains only digits
        return code != null && !code.isEmpty() && code.matches("\\d+");
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a new password", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check for minimum length (at least 8 characters)
        if (password.length() < 8) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            Toast.makeText(requireContext(), "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            Toast.makeText(requireContext(), "Password must contain at least one lowercase letter", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            Toast.makeText(requireContext(), "Password must contain at least one number", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            Toast.makeText(requireContext(), "Password must contain at least one special character", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void sendForgotPasswordRequest(String email) {
        // Create request body
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        // Make API call using Retrofit
        RetrofitClient.getInstance()
                .getApi()
                .forgotPassword(request)
                .enqueue(new Callback<ForgotPasswordResponse>() {
                    @Override
                    public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                        // Re-enable button
                        sendVerificationButton.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            ForgotPasswordResponse forgotPasswordResponse = response.body();

                            if (forgotPasswordResponse.isSuccess()) {
                                // Success - show verification layer
                                Toast.makeText(requireContext(), "Verification code sent to " + email, Toast.LENGTH_SHORT).show();
                                showVerificationLayer();
                            } else {
                                // API returned success=false
                                Toast.makeText(requireContext(), forgotPasswordResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle error response
                            try {
                                if (response.code() == 404) {
                                    Toast.makeText(requireContext(), "User with this email not found", Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 400) {
                                    Toast.makeText(requireContext(), "Invalid email format", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(requireContext(), "Error processing response", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                        // Re-enable button
                        sendVerificationButton.setEnabled(true);

                        // Network or other error
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void verifyCode(String code, String newPassword) {
        // Disable button to prevent multiple clicks
        resetButton.setEnabled(false);

        // Get the email that was entered
        String email = emailInput.getText().toString().trim();

        // Create request for reset password
        ResetPasswordRequest request = new ResetPasswordRequest(email, code, newPassword);

        // Make API call to reset password
        RetrofitClient.getInstance()
                .getApi()
                .resetPassword(request)
                .enqueue(new Callback<ResetPasswordResponse>() {
                    @Override
                    public void onResponse(Call<ResetPasswordResponse> call, Response<ResetPasswordResponse> response) {
                        // Re-enable button
                        resetButton.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            ResetPasswordResponse resetResponse = response.body();

                            if (resetResponse.isSuccess()) {
                                // Password reset successful
                                Toast.makeText(requireContext(), "Password reset successfully!", Toast.LENGTH_SHORT).show();

                                // Navigate to login page
                                if (requireActivity() instanceof AuthActivity) {
                                    AuthActivity authActivity = (AuthActivity) requireActivity();

                                    // Navigate to login fragment
                                    LoginFragment loginFragment = new LoginFragment();
                                    authActivity.getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.auth_container, loginFragment)
                                            .commit();

                                    Log.d(TAG, "Successfully reset password, navigating to LoginFragment");
                                }
                            } else {
                                // API returned success=false
                                Toast.makeText(requireContext(), resetResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle error response
                            try {
                                if (response.code() == 400) {
                                    Toast.makeText(requireContext(), "Invalid request data", Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 401) {
                                    Toast.makeText(requireContext(), "Invalid verification code", Toast.LENGTH_SHORT).show();
                                } else if (response.code() == 404) {
                                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(requireContext(), "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(requireContext(), "Error processing response", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResetPasswordResponse> call, Throwable t) {
                        // Re-enable button
                        resetButton.setEnabled(true);

                        // Network or other error
                        Toast.makeText(requireContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Reset password API call failed: " + t.getMessage());
                    }
                });
    }
}