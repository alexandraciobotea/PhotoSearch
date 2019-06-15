package ro.upt.cs.photosearch.entities;

import android.location.Location;

/**
 * Interface used for Location Callbacks from LocationMgr -> LocationUser Activity
 */
public interface LocationUser {

    /**
     * Called by the LocationManager when the location has significantly changed
     *
     * @param location - GPS Location
     */
    void onNewLocation(Location location);

    /**
     * Called by the LocationManager when all of the location providers become disabled.
     */
    void onAllProvidersDisabled();

    /**
     * Called by the LocationManager, when we don't have a location that is accurate enough. (partial fix)
     *
     * @param location - GPS Location
     */
    void onNotAccurateNewLocation(Location location);

    /**
     * Called by the LocationManager when a new provider was enabled
     *
     * @param provider - Name of the provider
     */
    void onProviderEnabled(String provider);
}
