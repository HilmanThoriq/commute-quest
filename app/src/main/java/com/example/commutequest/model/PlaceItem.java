package com.example.commutequest.model;


import com.google.gson.annotations.SerializedName;

public class PlaceItem {
    @SerializedName("coords")
    private Coordinates coords;

    @SerializedName("placeId")
    private String placeId;

    @SerializedName("text")
    private String text;

    public Coordinates getCoords() {
        return coords;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getText() {
        return text;
    }

    public static class Coordinates {
        @SerializedName("latitude")
        private double latitude;

        @SerializedName("longitude")
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }
    }
}