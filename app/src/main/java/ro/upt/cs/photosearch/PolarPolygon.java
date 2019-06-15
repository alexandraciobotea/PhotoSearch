package ro.upt.cs.photosearch;



import java.util.LinkedList;

import ro.upt.cs.photosearch.entities.Azimuth;
import ro.upt.cs.photosearch.entities.PolarPoint;

/**
 * A polygon represented with points in polar coordinates
 */
public class PolarPolygon extends LinkedList<PolarPoint> {
    /**
     * When drawing the polygon's side between two points, determines if the drawing happens
     * in the positive direction of the angles.
     *
     * @param a The point from where we are drawing
     * @param b The point we are drawing to
     * @return When drawing the line between the two points, if the draw will happen from a to b (from the point of view of the angles) returns true.
     */
    public static boolean isDrawingInPositiveDirection(PolarPoint a, PolarPoint b) {
        // The drawing will always happen on the shortest path between the two points.
        // This means that when considering polar angles, the drawing will happen in the direction of the smallest angle between the two.
        // As the sum(angle(a,b), angle(b,a)) = 360 deg, we only need to check if one of the values is less then 180 deg to find the smallest value.
        return a.getAzimuth().deltaTo(b.getAzimuth()) < 180; //deltaTo() implemented in Azimuth class
    }

    /**
     * Get a sub region polygon which is inside the given angle pair.
     */
    public PolarPolygon getRegionBetweenAngles(Azimuth minAngle, Azimuth maxAngle) {
        PolarPolygon subRegion = new PolarPolygon();
        // TODO would be nice to have shortcuts for cases when the whole poly is inside or completely outside

        if (!isEmpty()) {
            // the previous point is the last one, because the polygon is a closed shape.
            PolarPoint previousPoint = getLast();
            boolean isPreviousPointInside =
                    previousPoint.getAzimuth().isInsideAzimuthPair(minAngle, maxAngle);

            for (PolarPoint p : this) {
                boolean isInside = p.getAzimuth().isInsideAzimuthPair(minAngle, maxAngle);

                if (isInside) {
                    if (!isPreviousPointInside) {
                        // entering viewable azimuth angle
                        PolarPoint enteringPoint = getIntersectionPoint(previousPoint, p, minAngle, maxAngle);
                        subRegion.add(enteringPoint);
                    }

                    // the current point is inside, so it is part of the sub-region
                    subRegion.add(p);
                } else if (isPreviousPointInside) {
                    // leaving viewable azimuth area
                    PolarPoint leavingPoint = getIntersectionPoint(previousPoint, p, minAngle, maxAngle);
                    subRegion.add(leavingPoint);
                } else {
                    // in this case we need to determine if the line defined by the current and the previous point
                    // passes through the area defined by the two angles.
                    boolean auxPointsNeeded = isLinePassingThroughAngle(previousPoint, p, minAngle);

                    if (auxPointsNeeded) {
                        PolarPoint enteringPoint = getIntersectionPoint(previousPoint, p, minAngle, minAngle);
                        PolarPoint leavingPoint = getIntersectionPoint(previousPoint, p, maxAngle, maxAngle);
                        subRegion.add(enteringPoint);
                        subRegion.add(leavingPoint);
                    }
                }

                // iterate the previous point
                previousPoint = p;
                isPreviousPointInside = isInside;
            }
        }

        return subRegion;
    }

    /**
     * Used by {@link #getRegionBetweenAngles(Azimuth, Azimuth)}
     *
     * @return The entering or leaving point to the area defined by the two angles, when the current line
     * comes from p1, and goes towards p2. One of the two points should fall inside the area.
     */
    private PolarPoint getIntersectionPoint(PolarPoint p1, PolarPoint p2, Azimuth minAngle, Azimuth maxAngle) {

        if (!isDrawingInPositiveDirection(p1, p2)) {
            // switch p1 with p2, so we can have a drawing direction from p1 to p2
            PolarPoint t = p1;
            p1 = p2;
            p2 = t;
        }

        PolarPoint intersectionPoint = Azimuth.getLineAzimuthIntersectionPoint(p1, p2, minAngle);
        if (!intersectionPoint.getAzimuth().isInsideAzimuthPair(p1.getAzimuth(), p2.getAzimuth())) {
            intersectionPoint = Azimuth.getLineAzimuthIntersectionPoint(p1, p2, maxAngle);
        }

        return intersectionPoint;
    }

    /**
     * Determines if the line defined by the points passes though the line from the origin point
     * towards the angle
     *
     * @param p1    One of the points of the line
     * @param p2    An other point of the line
     * @param angle The angle
     */
    private boolean isLinePassingThroughAngle(PolarPoint p1, PolarPoint p2, Azimuth angle) {
        // will the polygon draw in positive direction? - from the point of view of the angles
        boolean drawingUpwards = isDrawingInPositiveDirection(p1, p2);

        double prevDeltaToMin = p1.getAzimuth().deltaTo(angle);
        double currentDeltaToMin = p2.getAzimuth().deltaTo(angle);

        if (drawingUpwards) {
            // drawing in the positive angle-sense (in math counter-clockwise, but in navigation this seems to be clockwise)
            return prevDeltaToMin < currentDeltaToMin;
        } else {
            // draw direction is from current point to the previous point
            return prevDeltaToMin > currentDeltaToMin;
        }
    }
}
