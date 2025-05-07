package com.example.commutequest.model;

public class LoginRequest {
    private String email;
    private String password;
    private String device_name;

    public LoginRequest(String email, String password, String deviceName) {
        this.email = email;
        this.password = password;
        this.device_name = deviceName;

    }
}
