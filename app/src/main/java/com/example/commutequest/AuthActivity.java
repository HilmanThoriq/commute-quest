package com.example.commutequest;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AuthActivity extends AppCompatActivity {
    private static final String TAG = "AuthActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Check if we need to open the ResetPasswordFragment directly
        if (getIntent().getBooleanExtra("OPEN_RESET_PASSWORD", false)) {
            String email = getIntent().getStringExtra("EMAIL");
            String code = getIntent().getStringExtra("CODE");

            Log.d(TAG, "Opening ResetPasswordFragment directly with email: " + email);

            // Create and show the ResetPasswordFragment
            ResetPasswordFragment resetPasswordFragment = ResetPasswordFragment.newInstance(email, code);
            loadFragment(resetPasswordFragment, true);
        }
        // If not coming from verification, show the default fragment
        else if (savedInstanceState == null) {
            // Load the login fragment by default
            loadFragment(new LoginFragment(), false);
        }
    }

    // Method to load a fragment into the container
    public void loadFragment(Fragment fragment) {
        loadFragment(fragment, false);
    }

    // Overloaded method with option to add to back stack
    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.auth_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    // Method to navigate to register fragment
    public void navigateToRegister() {
        loadFragment(new RegisterFragment());
    }

    // Method to navigate to login fragment
    public void navigateToLogin() {
        loadFragment(new LoginFragment());
    }

    public void navigateToVerify() {
        loadFragment(new VerifyFragment(), true);
    }

    // Method to navigate to reset password fragment
    public void navigateToResetPassword(String email, String code) {
        ResetPasswordFragment resetPasswordFragment = ResetPasswordFragment.newInstance(email, code);
        loadFragment(resetPasswordFragment, true);
    }
}