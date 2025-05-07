package com.example.commutequest.model;

import com.google.gson.annotations.SerializedName;

public class UpdateProfileRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    public UpdateProfileRequest(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}