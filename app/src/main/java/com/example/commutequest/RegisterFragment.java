package com.example.commutequest;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.commutequest.api.APIService;
import com.example.commutequest.api.RetrofitClient;
import com.example.commutequest.model.AuthResponse;
import com.example.commutequest.model.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText noHandphoneEditText;
    private EditText passwordEditText;
    private CheckBox showPasswordCheckBox;
    private Button registerButton;
    private View progressBar;
    private APIService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Initialize API Service
        apiService = RetrofitClient.getInstance().getApi();

        // Initialize views
        nameEditText = view.findViewById(R.id.name_input);
        emailEditText = view.findViewById(R.id.email_input);
        noHandphoneEditText = view.findViewById(R.id.phone_input);
        passwordEditText = view.findViewById(R.id.password_input);
        showPasswordCheckBox = view.findViewById(R.id.show_password_checkbox);
        registerButton = view.findViewById(R.id.register_button);

        // Add progress bar if it doesn't exist in your layout
        // progressBar = view.findViewById(R.id.progress_bar);

        // Set up click listeners
        registerButton.setOnClickListener(v -> attemptRegister());

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

        return view;
    }

    private void attemptRegister() {
        // Get input values
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = noHandphoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty()) {
            nameEditText.setError("Name cannot be empty.");
            return;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Email cannot be empty.");
            return;
        }

        if (phone.isEmpty()) {
            noHandphoneEditText.setError("Phone number cannot be empty.");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password cannot be empty.");
            return;
        }

        // Show progress
        showLoading(true);

        // Create register request
        RegisterRequest registerRequest = new RegisterRequest(name, email, phone, password);

        // Make API call
        apiService.register(registerRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    if (authResponse.isSuccess()) {
                        // Show success message
                        Toast.makeText(getContext(), "Registration successful. Please log in.", Toast.LENGTH_SHORT).show();

                        // Navigate back to login page after successful registration
                        if (getActivity() instanceof AuthActivity) {
                            ((AuthActivity) getActivity()).navigateToLogin();
                        }
                    } else {
                        // Show error message
                        String errorMsg = authResponse.getMessage();
                        if (authResponse.getError() != null) {
                            errorMsg = authResponse.getError().getMessage();
                        }
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Parse error response
                    try {
                        if (response.errorBody() != null) {
                            Toast.makeText(getContext(), "Registration failed. The email may already be registered.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "An error occurred. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Connection failed. Please check your internet connection.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        // If you have a progress bar in your layout, use this code
        // if (progressBar != null) {
        //     progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        // }

        registerButton.setEnabled(!isLoading);
        registerButton.setText(isLoading ? "Registering..." : "Register");
    }
}