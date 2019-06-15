package ro.upt.cs.photosearch.entities;


import java.util.LinkedList;

import ro.upt.cs.photosearch.PolarPolygon;


/**
 * Polygon
 */
public class Polygon extends LinkedList<Point> {

    /**
     * Convert the polygon to a polygon represented in polar coordinates
     *
     * @param originLongitude The origin of the coordinate system
     */
    public PolarPolygon toPolarPolygon(double originLongitude, double originLatitude) {
        PolarPolygon poly = new PolarPolygon();
        for (Point p : this) {
            poly.add(new PolarPoint(originLongitude, originLatitude, p.getLongitude(), p.getLatitude()));
        }
        return poly;
    }
}
