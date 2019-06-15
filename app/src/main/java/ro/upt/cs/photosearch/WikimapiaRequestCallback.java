package ro.upt.cs.photosearch;


/**
 * Interface for Wikimapia Request
 * When a request finished, the listener will be notified with new places
 */
public interface WikimapiaRequestCallback {

    /**
     * Forwards the SearchResult object to the listener class
     * Call .getPlaces() to get the list of available places
     *
     * @param result   SearchResult
     * @param location GPS Location
     */
    public void onMapChanged(SearchResult result, android.location.Location location);
}