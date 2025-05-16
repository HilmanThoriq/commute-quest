package com.example.commutequest.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AutocompleteResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<PlaceItem> data;

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<PlaceItem> getData() {
        return data;
    }
}
