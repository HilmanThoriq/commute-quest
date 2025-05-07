package com.example.commutequest.model;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private UserData data;

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public UserData getData() {
        return data;
    }

    public static class UserData {
        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("phone")
        private String phone;

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }
    }
}