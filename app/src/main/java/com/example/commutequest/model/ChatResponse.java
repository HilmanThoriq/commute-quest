package com.example.commutequest.model;

import com.google.gson.annotations.SerializedName;

public class ChatResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ChatData data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ChatData getData() {
        return data;
    }

    public void setData(ChatData data) {
        this.data = data;
    }

    /**
     * Nested class for the data object in the response
     */
    public static class ChatData {
        @SerializedName("question")
        private String question;

        @SerializedName("answer")
        private String answer;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
}
