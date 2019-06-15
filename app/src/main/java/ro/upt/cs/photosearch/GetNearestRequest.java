package ro.upt.cs.photosearch;

/**
 * Get nearest places to the given point
 */
public class GetNearestRequest extends WikimapiaRequest {
    public static final String FUNCTION_NAME = "place.getnearest";
    public static final String LONGITUDE = "lon";
    public static final String LATITUDE = "lat";

    public GetNearestRequest(WikimapiaClient client, double longitude, double latitude) {
        super(client);
        this.addParameter(LONGITUDE, Double.toString(longitude));
        this.addParameter(LATITUDE, Double.toString(latitude));

    }

    @Override
    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}