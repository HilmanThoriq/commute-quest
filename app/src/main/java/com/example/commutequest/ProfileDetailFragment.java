package com.example.commutequest;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.commutequest.api.APIService;
import com.example.commutequest.api.RetrofitClient;
import com.example.commutequest.model.ProfileResponse;
import com.example.commutequest.model.UpdateProfileRequest;
import com.example.commutequest.model.UpdateProfileResponse;
import com.example.commutequest.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileDetailFragment extends Fragment {
    private static final String TAG = "ProfileDetailFragment";

    private EditText nameInput, emailInput, phoneInput;
    private Button updateButton;
    private ImageView backButton;
    private SessionManager sessionManager;

    public ProfileDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        nameInput = view.findViewById(R.id.name_input);
        emailInput = view.findViewById(R.id.email_input);
        phoneInput = view.findViewById(R.id.phone_input);
        updateButton = view.findViewById(R.id.update_button);
        backButton = view.findViewById(R.id.back_button);

        // Initialize session manager
        sessionManager = new SessionManager(requireContext());

        // Set up focus change listeners for EditText fields
        setupFocusChangeListener(nameInput);
        setupFocusChangeListener(emailInput);
        setupFocusChangeListener(phoneInput);

        // Fill fields with existing data
        loadProfileData();

        // Set click listeners
        backButton.setOnClickListener(v -> {
            try {
                // Cara 1: Menggunakan FragmentManager untuk kembali ke fragment sebelumnya
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            } catch (Exception e) {
                // Log error jika terjadi masalah
                Log.e(TAG, "Error navigating back: " + e.getMessage(), e);

                // Cara 2: Jika popBackStack() tidak berfungsi, gunakan onBackPressed()
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }

        });

        updateButton.setOnClickListener(v -> {
            updateProfile();
        });
    }

    private void setupFocusChangeListener(EditText editText) {
        editText.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                // When EditText gets focus (is clicked for editing)
                // Change text color to black
                editText.setTextColor(getResources().getColor(R.color.font_black));
            } else {
                // When EditText loses focus
                if (editText.getText().toString().isEmpty()) {
                    // If the field is empty, let it show the hint color
                    editText.setTextColor(getResources().getColor(R.color.font_placeholder_grey));
                } else {
                    // Keep text black if there's content
                    editText.setTextColor(getResources().getColor(R.color.font_black));
                }
            }
        });
    }

    private void loadProfileData() {
        // Get data from session manager
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String phone = sessionManager.getUserPhone();

        // Set data to input fields
        if (name != null) nameInput.setText(name);
        if (email != null) emailInput.setText(email);
        if (phone != null) phoneInput.setText(phone);
    }

    private void updateProfile() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Validate input
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create request body
        UpdateProfileRequest request = new UpdateProfileRequest(name, email, phone);

        // Get auth header
        String authHeader = sessionManager.getAuthorizationHeader();
        if (authHeader == null) {
            Toast.makeText(requireContext(), "Token autentikasi tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        // Make API call
        APIService apiService = RetrofitClient.getInstance().getApi();
        Call<UpdateProfileResponse> call = apiService.updateProfile(authHeader, request);

        call.enqueue(new Callback<UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UpdateProfileResponse updateResponse = response.body();

                    if (updateResponse.isStatus()) {
                        // Update session data
                        sessionManager.saveUserData(name, email, phone);

                        Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();

                        // Navigate back
                        Navigation.findNavController(requireView()).navigateUp();
                    } else {
                        Toast.makeText(requireContext(), updateResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorMessage = "Gagal memperbarui profil";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                Log.e(TAG, "Network error when updating profile", t);
                Toast.makeText(requireContext(), "Gagal terhubung ke server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}