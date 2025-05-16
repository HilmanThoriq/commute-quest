
// 2. Add the response class
package com.example.commutequest.model;

public class MapsLinkResponse {
    private boolean success;
    private int code;
    private String message;
    private MapsLinkData data;

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public MapsLinkData getData() {
        return data;
    }

    public class MapsLinkData {
        private String link;

        public String getLink() {
            return link;
        }
    }
}
