package ro.upt.cs.photosearch;


import ro.upt.cs.photosearch.entities.Place;
import ro.upt.cs.photosearch.entities.PolarPoint;


public class PlaceObject {
    private Place place;
    private PolarPolygon polygon;

    public PlaceObject(Place place, double originLongitude, double originLatitude) {
        this.place = place;
        polygon = place.getPolygon().toPolarPolygon(originLongitude, originLatitude);
    }

    /**
     * Get the name that will be displayed on the UI.
     */
    public String getName() {
        return place.getTitle();
    }

    /**
     * @return The polygon which represents th
     */
    public PolarPolygon getPolygon() {
        return polygon;
    }


    public Place getPlace() {
        return place;
    }

    /**
     * A dummy place object.
     * Can be used for testing
     */
    public static class DummyPlaceObject extends PlaceObject {
        private String name;
        private double azimuth;

        public DummyPlaceObject(String name, double azimuth) {
            super(new Place(), 0, 0);
            this.name = name;
            this.azimuth = azimuth;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public PolarPolygon getPolygon() {
            PolarPolygon poly = new PolarPolygon();
            poly.add(new PolarPoint(10, azimuth - 10));
            poly.add(new PolarPoint(10, azimuth + 10));
            return poly;
        }
    }
}
