package com.example.commutequest.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RouteResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private RouteData data;

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public RouteData getData() {
        return data;
    }

    public static class RouteData {
        @SerializedName("routes")
        private List<Route> routes;

        public List<Route> getRoutes() {
            return routes;
        }
    }

    public static class Route {
        @SerializedName("distanceMeters")
        private int distanceMeters;

        @SerializedName("duration")
        private String duration;

        @SerializedName("polyline")
        private Polyline polyline;

        public int getDistanceMeters() {
            return distanceMeters;
        }

        public String getDuration() {
            return duration;
        }

        public Polyline getPolyline() {
            return polyline;
        }
    }

    public static class Polyline {
        @SerializedName("encodedPolyline")
        private String encodedPolyline;

        public String getEncodedPolyline() {
            return encodedPolyline;
        }
    }
}