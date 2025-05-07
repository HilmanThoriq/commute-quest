package com.example.commutequest;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.commutequest.api.APIService;
import com.example.commutequest.api.RetrofitClient;
import com.example.commutequest.model.AuthResponse;
import com.example.commutequest.model.LoginRequest;
import com.example.commutequest.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";

    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox showPasswordCheckBox;
    private Button signInButton;
    private TextView forgotPasswordText;
    private TextView registerText;
    private View progressBar;
    private SessionManager sessionManager;
    private APIService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize API Service
        apiService = RetrofitClient.getInstance().getApi();

        // Initialize SessionManager
        sessionManager = new SessionManager(requireContext());

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMainActivity();
            return view;
        }

        // Initialize views
        emailEditText = view.findViewById(R.id.email_input);
        passwordEditText = view.findViewById(R.id.password_input);
        showPasswordCheckBox = view.findViewById(R.id.show_password_checkbox);
        signInButton = view.findViewById(R.id.sign_in_button);
        forgotPasswordText = view.findViewById(R.id.forgot_password_text);
        registerText = view.findViewById(R.id.register_text);
        // Uncomment this if you have a progress bar in your layout
        // progressBar = view.findViewById(R.id.progress_bar);

        // Set up click listeners
        signInButton.setOnClickListener(v -> attemptLogin());

        // Show password checkbox toggle
        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show password
                passwordEditText.setTransformationMethod(null);
            } else {
                // Hide password
                passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
            }
            // Move cursor to the end
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Navigate to register fragment
        registerText.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).navigateToRegister();
            }
        });

        // Forgot password
        forgotPasswordText.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).navigateToVerify();
            }
        });

        return view;
    }

    private void attemptLogin() {
        // Get input values
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String deviceName = Build.MODEL;

        // Validate inputs
        if (email.isEmpty()) {
            emailEditText.setError("Email cannot be empty");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password cannot be empty");
            return;
        }

        // Show progress
        showLoading(true);

        // Create login request
        LoginRequest loginRequest = new LoginRequest(email, password, deviceName);

        // Make API call
        apiService.login(loginRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    if (authResponse.isSuccess() && authResponse.getData() != null) {
                        // Log successful login (for debugging)
                        Log.d(TAG, "Login successful. Token received.");

                        // Save token securely
                        sessionManager.saveAuthUser(authResponse.getData());

                        // Show success message
                        Toast.makeText(getContext(), authResponse.getMessage(), Toast.LENGTH_SHORT).show();

                        // Navigate to main activity
                        navigateToMainActivity();
                    } else {
                        // Show error message
                        String errorMsg = authResponse.getMessage();
                        if (authResponse.getError() != null) {
                            errorMsg = authResponse.getError().getMessage();
                        }
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Login failed: " + errorMsg);
                    }
                } else {
                    // Parse error response
                    try {
                        if (response.errorBody() != null) {
                            Toast.makeText(getContext(), "Login failed. Please check your email and password.",
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Login failed with HTTP code: " + response.code());
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "An error occurred. Please try again.",
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Exception during login error handling", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Connection failed. Please check your internet connection.",
                        Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Network failure during login", t);
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showLoading(boolean isLoading) {
        // If you have a progress bar in your layout, uncomment this code
        // if (progressBar != null) {
        //     progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // }

        signInButton.setEnabled(!isLoading);
        signInButton.setText(isLoading ? "Please wait ..." : "Sign In");
    }
}