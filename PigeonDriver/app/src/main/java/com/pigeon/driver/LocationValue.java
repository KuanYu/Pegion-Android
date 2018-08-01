package com.pigeon.driver;

/**
 * Created by Chalitta Khampachua on 06-Oct-17.
 */

public class LocationValue {
    private static double Latitude;
    private static double Longitude;

    public void setLatitude(double latitude){
        Latitude = latitude;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLongitude(double longitude) {
        Longitude = longitude;
    }

    public double getLongitude() {
        return Longitude;
    }
}
