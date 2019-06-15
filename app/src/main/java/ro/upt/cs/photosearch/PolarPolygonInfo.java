package ro.upt.cs.photosearch;



import android.support.annotation.NonNull;

import ro.upt.cs.photosearch.entities.Azimuth;
import ro.upt.cs.photosearch.entities.PolarPoint;

/**
 * Calculates information about a polarPolygon
 */
public class PolarPolygonInfo {
    private Azimuth minAzimuth;
    private Azimuth maxAzimuth;
    private double distanceSum = 0;
    private int distanceNum = 0;
    private boolean isEmpty = false;
    private boolean isSurroundingOriginPoint = false;

    private PolarPolygonInfo() {
    }

    /**
     * Create a {@link ro.upt.cs.photosearch.PolarPolygonInfo} for a polygon.
     * A call to this method will calculate all of the needed properties.
     *
     * @param polygon The polygon to calculate the values for
     */
    public static PolarPolygonInfo forPolygon(@NonNull PolarPolygon polygon) {
        PolarPolygonInfo info = new PolarPolygonInfo();
        if (polygon.isEmpty()) {
            info.isEmpty = true;
        } else {
            info.minAzimuth = polygon.getFirst().getAzimuth();
            info.maxAzimuth = polygon.getFirst().getAzimuth();
            PolarPoint previousPoint = polygon.getLast();
            for (PolarPoint p : polygon) {
                if (PolarPolygon.isDrawingInPositiveDirection(previousPoint, p)) {
                    info.addAzimuthAsMaxCandidate(p.getAzimuth());
                } else {
                    info.addAzimuthAsMinCandidate(p.getAzimuth());
                }
                info.addDistance(p.getDistance());
            }
            if (info.minAzimuth.equals(info.maxAzimuth)) {
                info.isSurroundingOriginPoint = true;
            }
        }
        return info;
    }

    /**
     * Adds a possible min azimuth value
     */
    private void addAzimuthAsMinCandidate(Azimuth azimuth) {
        if (azimuth.deltaTo(minAzimuth) < 180) {
            minAzimuth = azimuth;
        }
    }

    /**
     * Adds a possible max azimuth value.
     */
    private void addAzimuthAsMaxCandidate(Azimuth azimuth) {
        if (maxAzimuth.deltaTo(azimuth) < 180) {
            maxAzimuth = azimuth;
        }
    }

    /**
     * Add a distance value
     */
    private void addDistance(double distance) {
        distanceSum += distance;
        distanceNum++;
    }

    /**
     * Get the average distance of the polygon to the origin point
     */
    public double getDistance() {
        return distanceSum / distanceNum;
    }

    /**
     * Get the minAzimuth of the polygon
     */
    public Azimuth getMinAzimuth() {
        return minAzimuth;
    }

    /**
     * Get the max azimuth of the polygon
     */
    public Azimuth getMaxAzimuth() {
        return maxAzimuth;
    }

    /**
     * @return True if the polygon completely surrounds the origin point
     */
    public boolean isSurroundingOriginPoint() {
        return isSurroundingOriginPoint;
    }

    /**
     * Returns true if the polygon is empty
     */
    public boolean isEmpty() {
        return isEmpty;
    }
}
