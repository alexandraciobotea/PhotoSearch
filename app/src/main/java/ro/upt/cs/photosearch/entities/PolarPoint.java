package ro.upt.cs.photosearch.entities;


import android.location.Location;


/**
 * A point in 2D, represented in polar coordinates
 */


public class PolarPoint {
    private double distance;
    private Azimuth azimuth;

    public PolarPoint() {
        this(0, 0);
    }

    /**
     * Create a PolarPoint that will be of the distance between the two points on Earth,
     * and of the azimuth at which the final point can be seen from the first one.
     */
    public PolarPoint(double originLongitude, double originLatitude, double finalLongitude, double finalLatitude) {
        float[] results = new float[2];
        Location.distanceBetween(originLatitude, originLongitude, finalLatitude, finalLongitude, results);
        this.distance = results[0];
        this.azimuth = new Azimuth(results[1]);
    }

    public PolarPoint(double distance, double azimuth) {
        this(distance, new Azimuth(azimuth));
    }

    public PolarPoint(double distance, Azimuth azimuth) {
        this.distance = distance;
        this.azimuth = azimuth;
    }

    /**
     * @return The distance of the point from the origin
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Set the distance of the point from origin
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }

    /**
     * Get the azimuth of the point
     */
    public Azimuth getAzimuth() {
        return azimuth;
    }

    /**
     * Set the azimuth of the point
     */
    public void setAzimuth(Azimuth azimuth) {
        this.azimuth = azimuth;
    }

    /**
     * Convert to a Point represented in cartesian coordinates
     */
    public Point toPoint() {
        double angleInRadians = Math.toRadians(getAzimuth().getValue());
        double x = distance * Math.cos(angleInRadians);
        double y = distance * Math.sin(angleInRadians);
        return new Point(x, y);
    }
}
