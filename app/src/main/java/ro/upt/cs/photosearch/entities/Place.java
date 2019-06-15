package ro.upt.cs.photosearch.entities;

/**
 * Represents a place
 */
public class Place {
    private long id = 0;
    private String title = "";

    // Azimuth first for polar point
   // private Polygon polygon = new Polygon();
    private Polygon polygon = new Polygon();
    private Location location = new Location();
    private int distance = 0;

    public long getId() {
        return id;
    }

    /**
     * @return Title of the place
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return The polygon which marks the boundaries of the place
     */



    public Polygon getPolygon() {
        return polygon;
    }




    /**
     * @return The location of the place
     */
    public Location getLocation() {
        return location;
    }

    /**
     * The distance to the given place
     */
    public int getDistance() {
        return distance;
    }

    /**
     * @return The string representation of the place. (the title)
     */
    @Override
    public String toString() {
        return getTitle();
    }
}
