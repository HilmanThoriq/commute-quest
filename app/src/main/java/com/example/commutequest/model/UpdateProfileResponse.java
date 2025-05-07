package com.example.commutequest.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("message")
    private String message;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}