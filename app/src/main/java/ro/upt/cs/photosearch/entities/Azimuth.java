package ro.upt.cs.photosearch.entities;


/**
 * Holder class for azimuth value
 */
public class Azimuth {

    /**
     *
     * Real value of the azimuth in degrees(0 - 360)
     */
    private double azimuth;

    /**
     * Constructor
     * The value provided will be shifted with 180 degrees
     *
     * @param azimuth - horizontal direction of the phone
     */
    public Azimuth(double azimuth) {
        setValue(azimuth);
    }




    /**
     * Returns the intersection point between a line segment and an azimuth angle.
     * The line segment is defined by points a, and b
     *
     * @param p1       The first point of the line
     * @param p2       The second point of the line
     * @param azimuth The azimuth angle
     * @return The intersection point.
     */
    public static PolarPoint getLineAzimuthIntersectionPoint(PolarPoint p1, PolarPoint p2, Azimuth azimuth) {
        double alpha = Math.toRadians(p1.getAzimuth().deltaTo(p2.getAzimuth()));
        double omega = Math.toRadians(azimuth.getValue());
        if (alpha > Math.PI) {
            return getLineAzimuthIntersectionPoint(p2, p1, azimuth);
        } else {
            double b = p1.getDistance();
            double c = p2.getDistance();
            double a = Math.sqrt(b * b + c * c - 2 * b * c * Math.cos(alpha));
            double sinBeta = b * Math.sin(alpha) / a;
            double beta = Math.asin(sinBeta);
            double epsilon = Math.PI - omega - beta;
            double x = c * sinBeta / Math.sin(epsilon);
            return new PolarPoint(x, azimuth.getValue());
        }
    }

    /**
     * Checks two azimuth pairs for intersections.
     *
     * @param minAzimuth1 The min azimuth of the first pair
     * @param maxAzimuth1 The max azimuth of the first pair
     * @param minAzimuth2 The min azimuth of the second pair
     * @param maxAzimuth2 The max azimuth of the second pair
     * @return True if the two pairs intersect
     */
    public static boolean checkAzimuthPairsForIntersection(Azimuth minAzimuth1, Azimuth maxAzimuth1, Azimuth minAzimuth2, Azimuth maxAzimuth2) {
        // the intersection happens if one of the other min or max values is between the largest pair
        double d1 = minAzimuth1.deltaTo(maxAzimuth1);
        double d2 = minAzimuth2.deltaTo(maxAzimuth2);
        if (d1 > d2) {
            // the first pair is the largest.
            return minAzimuth2.isInsideAzimuthPair(minAzimuth1, maxAzimuth1)
                    || maxAzimuth2.isInsideAzimuthPair(minAzimuth1, maxAzimuth1);
        } else {
            return minAzimuth1.isInsideAzimuthPair(minAzimuth2, maxAzimuth2)
                    || maxAzimuth1.isInsideAzimuthPair(minAzimuth2, maxAzimuth2);
        }
    }

    /**
     * Public getter for azimuth value
     *
     * @return Azimuth
     */
    public double getValue() {
        return azimuth;
    }

    /**
     * Internal setter for azimuth value
     */
    public void setValue(double azimuth) {
        this.azimuth = azimuth;
        this.normalize();
    }

    /**
     * Normalizes the azimuth value to be between 0 and 360 degrees
     */
    private void normalize() {
        while (azimuth < 0) {
            azimuth += 360;
        }
        while (azimuth > 360) {
            azimuth -= 360;
        }
    }

    /**
     * Get the difference of angle between two azimuth values.
     * Starting from this value to the value in the parameter, in positive direction.
     *
     * @param a The other azimuth angle
     * @return The difference in degrees
     */
    public double deltaTo(Azimuth a) {
        if (a.azimuth >= azimuth) {
            return a.azimuth - azimuth;
        } else {
            return a.azimuth + 360 - azimuth;
        }
    }

    @Override
    public String toString() {
        return Double.toString(azimuth);
    }

    /**
     * Get the difference of angle between two azimuth values.
     * See {@link #deltaTo(Azimuth)}
     */
    public double deltaFrom(Azimuth a) {
        return a.deltaTo(this);
    }

    /**
     * Checks if this azimuth angle is between the given to angles.
     */
    public boolean isInsideAzimuthPair(Azimuth minAzimuth, Azimuth maxAzimuth) {
        if (minAzimuth.azimuth <= maxAzimuth.azimuth) {
            // simple case.
            return azimuth >= minAzimuth.azimuth
                    && azimuth <= maxAzimuth.azimuth;
        } else {
            return azimuth >= minAzimuth.azimuth
                    || azimuth <= maxAzimuth.azimuth;
        }
    }

    /**
     * Returns the amount the azimuth has changed when transitioning from the current value to the new value
     * This is different from {@link #deltaTo(Azimuth)}, because it assumes that the change can occur in any direction.
     *
     * @param newValue The value to compare to
     * @return A change of angle between -180 and 180 degrees
     */
    public double getChangeDelta(Azimuth newValue) {
        // how much did the angle change?
        double changeDelta;
        if (newValue.azimuth >= azimuth) {
            changeDelta = deltaTo(newValue);
        } else {
            changeDelta = -deltaFrom(newValue);
        }
        return changeDelta;
    }

    /**
     * Returns true if the difference between the two azimuths
     * are significant for the user interface
     *
     * @param newValue  Azimuth
     * @param minChange The minimum change needed
     * @return boolean
     */
    public boolean isAzimuthSignificantlyChanged(Azimuth newValue, float minChange) {
        double changeDelta = Math.abs(getChangeDelta(newValue));
        return (changeDelta > minChange);
    }
}
