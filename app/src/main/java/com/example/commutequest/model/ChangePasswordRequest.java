package com.example.commutequest.model;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordRequest {
    private String email;
    private String password;

    @SerializedName("reset_token")
    private String resetToken;

    public ChangePasswordRequest(String email, String password, String resetToken) {
        this.email = email;
        this.password = password;
        this.resetToken = resetToken;
    }

    // Getters and setters if needed
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
}