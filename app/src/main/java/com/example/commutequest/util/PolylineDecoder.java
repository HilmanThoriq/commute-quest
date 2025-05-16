package com.example.commutequest.util;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
/**
 * Utility class to decode polyline points from Google Maps API
 * Based on the algorithm described at:
 * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
 */
public class PolylineDecoder {

    /**
     * Decodes an encoded polyline string into a list of LatLng points
     *
     * @param encoded The encoded polyline string
     * @return A list of LatLng coordinates
     */
    public static List<LatLng> decode(String encoded) {
        List<LatLng> poly = new ArrayList<>();

        if (encoded == null || encoded.isEmpty()) {
            return poly;
        }

        int index = 0;
        int len = encoded.length();
        int lat = 0;
        int lng = 0;
        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;

            // Decode latitude
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            // Decode longitude
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            // Convert to decimal degrees (the factor 1e5 is from Google's encoding spec)
            double latitude = lat * 1e-5;
            double longitude = lng * 1e-5;

            // Add point to the polyline
            poly.add(new LatLng(latitude, longitude));
        }

        return poly;
    }

    /**
     * Encodes a list of LatLng points into an encoded polyline string
     * This method can be useful if you need to encode routes on the client side
     *
     * @param points The list of LatLng coordinates
     * @return An encoded polyline string
     */
    public static String encode(List<LatLng> points) {
        StringBuilder encodedPoints = new StringBuilder();

        if (points == null || points.isEmpty()) {
            return "";
        }

        int prevLat = 0;
        int prevLng = 0;

        for (LatLng point : points) {
            // Convert to integer representation (multiply by 1e5 and round)
            int lat = (int) Math.round(point.latitude * 1e5);
            int lng = (int) Math.round(point.longitude * 1e5);

            // Encode the differences
            encodeDifference(encodedPoints, lat - prevLat);
            encodeDifference(encodedPoints, lng - prevLng);

            prevLat = lat;
            prevLng = lng;
        }

        return encodedPoints.toString();
    }

    /**
     * Helper method to encode a single difference value
     *
     * @param builder StringBuilder to append to
     * @param value The difference value to encode
     */
    private static void encodeDifference(StringBuilder builder, int value) {
        // Left shift the value to make room for the sign bit
        value = value << 1;

        // Invert if negative
        if (value < 0) {
            value = ~value;
        }

        // Break into 5-bit chunks and convert to ASCII
        while (value >= 0x20) {
            builder.append((char) ((0x20 | (value & 0x1f)) + 63));
            value >>= 5;
        }

        builder.append((char) (value + 63));
    }
}