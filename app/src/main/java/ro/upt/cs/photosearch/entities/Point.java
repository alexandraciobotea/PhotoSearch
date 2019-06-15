package ro.upt.cs.photosearch.entities;


import com.google.gson.annotations.SerializedName;

/**
 * A point in 2D space
 */
public class Point {
    @SerializedName("x")
    private double longitude;
    @SerializedName("y")
    private double latitude;

    public Point() {
        this(0, 0);
    }

    public Point(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * @return The longitude coordinate of the point
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude coordinate of the point
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return The latitude coordinate of the point
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude coordinate of the point
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
