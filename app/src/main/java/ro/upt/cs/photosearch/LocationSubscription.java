package ro.upt.cs.photosearch;


import android.location.Location;
import android.util.Log;

import ro.upt.cs.photosearch.entities.LocationUser;

public class LocationSubscription {
    private static final String TAG = "LocationSubscription";

    private final LocationUser user;
    private Location lastKnownLocation = null;
    // The minimum distance between updates
    public int minUpdateDistance = 0;
    // The needed minimum accuracy
    public int minRequiredAccuracy = 5000;
    // The maximum time between updates (sec)
    public long maxUpdateTime = 600;
    // The last known location's time threshold in milliseconds
    public long timeDifferenceThreshold = 10000;

    public LocationSubscription(LocationUser user) {
        this.user = user;
    }

    public LocationUser getUser() {
        return user;
    }

    void handleNewLocation(Location location) {
        long distanceFromLast = getDistanceFromLastLocation(location);
        if (distanceFromLast >= minUpdateDistance && isWithinAllowedTimeThreshold(location)) {
            if (location.getAccuracy() <= minRequiredAccuracy) {
                // we have a nice new location
                Log.v(TAG, "The user will be notified of the new location");
                user.onNewLocation(location);
                lastKnownLocation = location;
            } else if (lastKnownLocation == null || maxUpdateTime < location.getTime() - lastKnownLocation.getTime()) {
                // we need to update even if the location is not accurate enough
                Log.v(TAG, String.format(
                        "The user %s will be notified of the not accurate enough new location. acc=%d, needed=%d",
                        user, Math.round(location.getAccuracy()), minRequiredAccuracy
                ));
                user.onNotAccurateNewLocation(location);
            }
        } else {
            Log.v(TAG,
                    String.format("The user was not notified. The distance %d is less then the minUpdateDistance %d.", distanceFromLast, minUpdateDistance));
        }
    }

    void onAllProvidersDisabled() {
        user.onAllProvidersDisabled();
    }

    void onProviderEnabled(String provider) {
        user.onProviderEnabled(provider);
    }

    /**
     * Get the distance from the last known location to the location specified
     * If we don't have a last known location, Long.MAX_VALUE will be returned
     */
    long getDistanceFromLastLocation(Location location) {
        if (lastKnownLocation == null) {
            return Long.MAX_VALUE;
        } else {
            return Math.round(location.distanceTo(lastKnownLocation));
        }
    }

    /**
     * Checks if a location is within the allowed time threshold.
     */
    boolean isWithinAllowedTimeThreshold(Location location) {
        if (lastKnownLocation == null) {
            return true;
        } else {
            long timeDifference = location.getTime() - lastKnownLocation.getTime();

            return (timeDifference > -timeDifferenceThreshold);
        }
    }

    /**
     * Set the minimum distance between updates
     */
    public void setMinUpdateDistance(int minUpdateDistance) {
        this.minUpdateDistance = minUpdateDistance;
        Log.v(TAG, String.format("The min update distance has been set to %d", minUpdateDistance));
    }

    /**
     * Set the minimum required accuracy (in meters) for an update to occur.
     */
    public void setMinRequiredAccuracy(int minRequiredAccuracy) {
        this.minRequiredAccuracy = minRequiredAccuracy;
        Log.v(TAG, String.format("The min required accuracy has been set to %d", minRequiredAccuracy));
    }

    /**
     * Set the maximum time between updates (in seconds)
     * This is only applied if we have moved by at least minUpdateDistance, but the new location is not accurate enough.
     */
    public void setMaxUpdateTime(int maxUpdateTime) {
        // convert to milliseconds
        this.maxUpdateTime = maxUpdateTime * 1000;
        Log.v(TAG, String.format("The max update time has been set to %d", maxUpdateTime));
    }



    /**
     * @return The last known location. (null if no location has been found.)
     */
    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }
}
