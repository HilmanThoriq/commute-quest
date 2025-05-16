package com.example.commutequest.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RouteRequest {
    private double originLatitude;
    private double originLongitude;
    private double destinationLatitude;
    private double destinationLongitude;

    public RouteRequest(double originLatitude, double originLongitude, double destinationLatitude, double destinationLongitude) {
        // Format with exactly 5 decimal places
        this.originLatitude = roundToFiveDecimalPlaces(originLatitude);
        this.originLongitude = roundToFiveDecimalPlaces(originLongitude);
        this.destinationLatitude = roundToFiveDecimalPlaces(destinationLatitude);
        this.destinationLongitude = roundToFiveDecimalPlaces(destinationLongitude);
    }

    // Helper method to round to exactly 5 decimal places
    private double roundToFiveDecimalPlaces(double value) {
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(5, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    // Getters and setters
    public double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(double originLatitude) {
        this.originLatitude = roundToFiveDecimalPlaces(originLatitude);
    }

    public double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(double originLongitude) {
        this.originLongitude = roundToFiveDecimalPlaces(originLongitude);
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = roundToFiveDecimalPlaces(destinationLatitude);
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = roundToFiveDecimalPlaces(destinationLongitude);
    }
}