package ro.upt.cs.photosearch.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a location in wikimapia
 */
public class Location {
    @SerializedName("lat")
    private double latitude = 0;

    @SerializedName("lon")
    private double longitude = 0;

    private String city = "";
    private String state = "";
    private String place = "";

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPlace() {
        return place;
    }
}