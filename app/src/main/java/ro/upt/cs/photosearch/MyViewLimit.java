package ro.upt.cs.photosearch;


import ro.upt.cs.photosearch.entities.Azimuth;

/**
 * Holder class for horizontal view limits, which is obtained from phone azimuth(direction)
 */
public class MyViewLimit {
    private static final String TAG = "ViewLimit";

    /**
     * Left view limit of the user
     */
    private Azimuth leftLimit;

    /**
     * Right view limit of the user
     */
    private Azimuth rightLimit;

    /**
     * Represents the ViewLimit center
     */
    private Azimuth frontAngle;

    /**
     * The width of the current view angle of the camera
     */
    private int viewAngleWidth;

    /**
     * The width of the display area in pixels
     */
    private int displayWidth;

    private ObjectDrawInfo drawInfo = new ObjectDrawInfo();

    /**
     * Represents the current view angle of the user
     *
     * @param azimuth        The azimuth the user is currently looking at
     * @param viewAngleWidth HorizontalViewAngle
     * @param displayWidth   The width on which we will draw in pixels
     */
    public MyViewLimit(Azimuth azimuth, int viewAngleWidth, int displayWidth) {
        // The angle width of the view
        this.viewAngleWidth = viewAngleWidth;
        setDisplayWidth(displayWidth);
        setFrontAngle(azimuth);
    }

    /**
     * Set the width of the view angle (the angle width of the camera)
     *
     * @param viewAngleWidth the camera's angle in degrees.
     */
    public void setViewAngleWidth(int viewAngleWidth) {
        this.viewAngleWidth = viewAngleWidth;
        recalculateLimits();
    }

    /**
     * Recalculates the left and right limits based on the view angle and the front angle
     */
    private void recalculateLimits() {
        double halfOfTheCameraAngle = ((double) viewAngleWidth) / 2;
        // Left limit is obtained by decreasing the current direction with the half of the Horizontal View Angle
        leftLimit = new Azimuth(frontAngle.getValue() - halfOfTheCameraAngle);
        // Right limit is obtained by increasing the current direction with the half of the Horizontal View Angle
        rightLimit = new Azimuth(frontAngle.getValue() + halfOfTheCameraAngle);
    }

    /**
     * Set the width of the displayed region (in pixels)
     */
    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    /**
     * Returns the left side view limit
     *
     * @return PolarAzimuth
     */
    public Azimuth getLeftLimit() {
        return leftLimit;
    }

    /**
     * Returns the right side view limit
     *
     * @return PolarAzimuth
     */
    public Azimuth getRightLimit() {
        return rightLimit;
    }

    /**
     * Returns the front angle in polar coordinates
     *
     * @return PolarAzimuth
     */
    public Azimuth getFrontAngle() {
        return frontAngle;
    }

    /**
     * Set the front angle of the view
     */
    public void setFrontAngle(Azimuth frontAngle) {
        this.frontAngle = frontAngle;
        recalculateLimits();
    }

    /**
     * Find out how many pixels does a given view angle would take
     *
     * @param angle The view angle to check
     * @return Tha amount of pixels which is taken by the given angle
     */
    public double getAngleInPixels(double angle) {
        return angle * displayWidth / viewAngleWidth;
    }

    /**
     * Get the draw info for a object between the given two azimuth values.
     * This method will re-use the returned instance for subsequent calls.
     *
     * @return Information on how the object should be drawn on the screen
     */
    public ObjectDrawInfo getDrawInfo(Azimuth minAzimuth, Azimuth maxAzimuth) {
        drawInfo.calculate(minAzimuth, maxAzimuth);
        return drawInfo;
    }

    /**
     * Holds, and calculates information about how an object should be drawn on the screen
     */
    public class ObjectDrawInfo {
        public int left;
        public int width;
        public boolean isInsideView;

        ObjectDrawInfo() {
        }

        public void calculate(Azimuth minAzimuth, Azimuth maxAzimuth) {
            isInsideView = Azimuth.checkAzimuthPairsForIntersection(minAzimuth, maxAzimuth, leftLimit, rightLimit);
            // the calculations are only needed if we are inside
            if (isInsideView) {
                Azimuth minDrawAzimuth;
                Azimuth maxDrawAzimuth;
                // get the meaningful min and max values for the current view angle
                if (leftLimit.deltaTo(minAzimuth) < 180) {
                    minDrawAzimuth = minAzimuth;
                } else {
                    minDrawAzimuth = leftLimit;
                }
                if (rightLimit.deltaTo(maxAzimuth) < 180) {
                    maxDrawAzimuth = rightLimit;
                } else {
                    maxDrawAzimuth = maxAzimuth;
                }
                double angleDiff = minDrawAzimuth.deltaTo(maxDrawAzimuth);
                double startAngle = leftLimit.deltaTo(minDrawAzimuth);

                left = (int) Math.round(getAngleInPixels(startAngle));
                width = (int) Math.round(getAngleInPixels(angleDiff));
            }
        }
    }
}
