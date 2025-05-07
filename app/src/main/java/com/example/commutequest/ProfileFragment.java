package com.example.commutequest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.commutequest.api.APIService;
import com.example.commutequest.api.RetrofitClient;
import com.example.commutequest.model.ProfileResponse;
import com.example.commutequest.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private TextView tvName;
    private ProgressBar progressBar;
    private SessionManager sessionManager;
    private LinearLayout menuProfile, menuChangePassword, menuLogout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Inisialisasi views
        tvName = view.findViewById(R.id.tv_profile_name);
        progressBar = view.findViewById(R.id.progress_bar);

        // Inisialisasi menu items
        menuProfile = view.findViewById(R.id.menu_profile);
        menuChangePassword = view.findViewById(R.id.menu_change_password);
        menuLogout = view.findViewById(R.id.menu_logout);

        // Inisialisasi SessionManager
        sessionManager = new SessionManager(requireContext());

        // Setup click listeners
        setupClickListeners();

        // Cek data lokal dulu
        displayLocalProfileData();

        // Ambil data terbaru dari server
        fetchProfileFromServer();

        return view;
    }

    private void setupClickListeners() {
        // Click listener untuk menu Profile
        menuProfile.setOnClickListener(v -> {
            navigateToProfileDetail();
        });

        // Click listener untuk menu Change Password
        menuChangePassword.setOnClickListener(v -> {
            navigateToResetPassword();
        });

        // Click listener untuk menu Logout
        menuLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void navigateToProfileDetail() {
        // Navigasi ke ProfileDetailFragment
        ProfileDetailFragment profileDetailFragment = new ProfileDetailFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, profileDetailFragment)
                .addToBackStack(null) // Add to back stack for back navigation
                .commit();
    }

    private void navigateToResetPassword() {
        // Navigasi ke ResetPasswordFragment
        ResetPasswordFragment resetPasswordFragment = new ResetPasswordFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, resetPasswordFragment)
                .addToBackStack(null) // Add to back stack for back navigation
                .commit();
    }

    private void logout() {
        // Clear session data
        sessionManager.clearSession();

        // Redirect to AuthActivity yang berisi LoginFragment
        Intent intent = new Intent(requireActivity(), AuthActivity.class);
        // Tambahkan flag untuk membersihkan stack activity sebelumnya
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Dapat menambahkan extra untuk menandai bahwa harus menampilkan LoginFragment
        intent.putExtra("SHOW_LOGIN", true);
        startActivity(intent);

        // Finish semua activity
        requireActivity().finishAffinity();
    }

    private void displayLocalProfileData() {
        String name = sessionManager.getUserName();

        // Tampilkan data dari local storage jika tersedia
        if (name != null && !name.isEmpty()) {
            tvName.setText(name);
        }
    }

    private void fetchProfileFromServer() {
        // Tampilkan loading
        progressBar.setVisibility(View.VISIBLE);

        // Cek apakah user sudah login
        if (!sessionManager.isLoggedIn()) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Anda belum login", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ambil token authorization
        String authHeader = sessionManager.getAuthorizationHeader();
        if (authHeader == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Token autentikasi tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buat API call
        APIService apiService = RetrofitClient.getInstance().getApi();
        Call<ProfileResponse> call = apiService.getUserProfile(authHeader);

        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profileResponse = response.body();

                    if (profileResponse.isSuccess() && profileResponse.getData() != null) {
                        // Ambil data user
                        ProfileResponse.UserData userData = profileResponse.getData();

                        // Simpan ke SessionManager
                        sessionManager.saveUserData(
                                userData.getName(),
                                userData.getEmail(),
                                userData.getPhone()
                        );

                        // Update UI
                        tvName.setText(userData.getName());

                        Log.d(TAG, "Profile data fetched successfully");
                    } else {
                        Log.e(TAG, "Error in profile response: " + profileResponse.getMessage());
                        Toast.makeText(requireContext(), profileResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle error response
                    String errorMessage = "Gagal mengambil data profil";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }

                    Log.e(TAG, "Error fetching profile: " + errorMessage);
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Network error when fetching profile", t);
                Toast.makeText(requireContext(), "Gagal terhubung ke server: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}