package com.example.commutequest.model;

public class ChangePasswordResponse {
    private boolean success;
    private String message;
    private String status;
    private Object data;

    public boolean isSuccess() {
        return success || (status != null && status.equalsIgnoreCase("success"));
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }
}