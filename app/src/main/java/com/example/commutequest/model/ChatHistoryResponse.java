package com.example.commutequest.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ChatHistoryResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private List<ChatEntry> data;

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<ChatEntry> getData() {
        return data;
    }

    public static class ChatEntry {
        @SerializedName("question")
        private String question;

        @SerializedName("answer")
        private String answer;

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
}