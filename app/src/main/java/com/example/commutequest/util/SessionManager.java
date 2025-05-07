package com.example.commutequest.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;


import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.commutequest.model.AuthResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {


    private static final String TAG = "SessionManager";

    // Shared Preferences File Name
    private static final String PREF_NAME = "CommuteQuestSecurePrefs";

    // Shared Preferences Keys
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHONE = "userPhone";

    private SharedPreferences securePreferences;
    private SharedPreferences.Editor secureEditor;

    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        try {
            // Create or retrieve the Master Key for encryption/decryption
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Create the EncryptedSharedPreferences
            securePreferences = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            secureEditor = securePreferences.edit();
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences", e);
            // Fallback to regular SharedPreferences in case of error
            securePreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            secureEditor = securePreferences.edit();

            // Notify about the security fallback
            Log.w(TAG, "Falling back to regular SharedPreferences due to security initialization error");
        }
    }

    /**
     * Save authentication data when user logs in
     */
    public void saveAuthUser(AuthResponse.TokenData tokenData) {
        if (tokenData != null && tokenData.getToken() != null) {
            secureEditor.putBoolean(KEY_IS_LOGGED_IN, true);
            secureEditor.putString(KEY_AUTH_TOKEN, tokenData.getToken());
            secureEditor.apply();

            Log.d(TAG, "User authentication data saved securely");
        }
    }

    /**
     * Save user data (name, email, phone)
     */
    public void saveUserData(String name, String email, String phone) {
        secureEditor.putString(KEY_USER_NAME, name);
        secureEditor.putString(KEY_USER_EMAIL, email);
        secureEditor.putString(KEY_USER_PHONE, phone);
        secureEditor.apply();
        Log.d(TAG, "User data saved securely");
    }

    /**
     * Save user data (name, email, phone, password)
     * @param name
     * @param email
     * @param phone
     * @param password
     */
    /*
    public void saveUserData(String name, String email, String phone, String password) {
        secureEditor.putString(KEY_USER_NAME, name);
        secureEditor.putString(KEY_USER_EMAIL, email);
        secureEditor.putString(KEY_USER_PHONE, phone);
        secureEditor.putString(KEY_USER_PASSWORD, password); // Jangan lakukan ini di aplikasi nyata
        secureEditor.apply();
        Log.d(TAG, "User data saved securely");
    }
    */

    /**
     * Get the authentication token for API requests
     */
    public String getAuthToken() {
        return securePreferences.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Get user name
     * @return
     */
    public String getUserName() {
        return securePreferences.getString(KEY_USER_NAME, null);
    }

    /**
     * Get user email
     * @return
     */
    public String getUserEmail() {
        return securePreferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Get user phone
     * @return
     */
    public String getUserPhone() {
        return securePreferences.getString(KEY_USER_PHONE, null);
    }


    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return securePreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Log the user out by clearing all session data
     */
    public void logout() {
        secureEditor.clear();
        secureEditor.apply();
        Log.d(TAG, "User logged out, all secure preferences cleared");
    }

    /**
     * Get authorization header value for API requests
     */
    public String getAuthorizationHeader() {
        String token = getAuthToken();
        if (token != null && !token.isEmpty()) {
            return "Bearer " + token;
        }
        return null;
    }

    public void clearSession() {
        // Clear all data from SharedPreferences
        secureEditor.clear();
        secureEditor.apply();
    }
}