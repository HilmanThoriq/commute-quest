package com.example.commutequest.model;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("reset_token")
    private String resetToken;

    @SerializedName("password")
    private String password;

    public ResetPasswordRequest(String email, String resetToken, String password) {
        this.email = email;
        this.resetToken = resetToken;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
