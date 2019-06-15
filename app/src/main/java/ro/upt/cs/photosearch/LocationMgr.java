package ro.upt.cs.photosearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.String;
import ro.upt.cs.photosearch.LocationSubscription;

public class LocationMgr implements LocationListener {
    private static LocationMgr ourInstance = null;
    // Common string used for logging
    private static final String TAG = "LOCATION";

    // List of providers, in order from which we want updates
    // The NETWORK_PROVIDER is the first one,
    // because location obtained from NETWORK_PROVIDER is less believable then the other one
    // It may be overwritten by a location from GPS_PROVIDER
    private final static String[] PROVIDERS = {LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};

    private final List<LocationSubscription> locationSubscriptions = new ArrayList<>();
    // List of provider to which are currently enabled
    private final List<String> enabledProviders = new ArrayList<>(PROVIDERS.length);
    @SystemService
    protected LocationManager locationManager;
    private boolean isUpdateRequested = false;
    // The minimum time between updates (sec)
    private long minUpdateTime = 0;

    public static LocationMgr getInstance(Context context) {
        if(ourInstance == null) {
            ourInstance = new LocationMgr(context);
        }
        return ourInstance;

    }

    private LocationMgr(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }


    /**
     * Subscribe for location updates
     * The updates are filtered as follows:
     * - when we have no new location (the location did not change by minDistance), no updates will be sent
     * - when we have moved by at least minDistance, and we have an accurate enough location, onNewLocation will be called
     * - when we have moved by at least minDistance, but we have no accurate reading on where we are for at least maxUpdateTime seconds, onNotAccurateNewLocation will be called
     * The initial update will be called by this method.
     */
    public void subscribeToProviders(LocationSubscription subscription) {
        if (locationSubscriptions.contains(subscription)) {
            throw new LocationMgr.DoubleSubscriptionException();
        }
        locationSubscriptions.add(subscription);

        requestUpdatesFromProviders();

        sendFirstUpdate(subscription);
    }

    @SuppressLint("MissingPermission")
    private void sendFirstUpdate(LocationSubscription subscription) {
        if (enabledProviders.isEmpty()) {
            Log.w(TAG, "User subscribed to updates, but we have no enabled providers.");
            subscription.getUser().onAllProvidersDisabled();
        } else {
            Location l = null;
            // check the providers to do a first update
            for (String providerName : enabledProviders) {
                l = locationManager.getLastKnownLocation(providerName);
            }
            Log.w(TAG, String.format("User subscribed to updates location=%s.", l));
            if (l != null) {
                subscription.handleNewLocation(l);
            }
        }
    }

    /**
     * Set the minimum time between updates (in seconds)
     */
    public void setMinUpdateTime(int minUpdateTime) {
        // convert to milliseconds
        this.minUpdateTime = minUpdateTime * 1000;
        Log.v(TAG, String.format("The min update time has been set to %d", this.minUpdateTime));
    }

    /**
     * Unsubscribe from location updates
     */
    public void unsubscribe(LocationSubscription subscription) {
        int index = -1;
        for (int i = 0; i < locationSubscriptions.size(); i++) {
            if (locationSubscriptions.get(i).equals(subscription)) {
                index = i;
            }
        }
        if (index != -1) {
            Log.i(TAG, "User unsubscribed from location.");
            locationSubscriptions.remove(index);
            if (locationSubscriptions.size() == 0) {
                stopUpdatesFromProviders();
            }
        }
    }

    /**
     * Requests updates from the location providers
     */
    @SuppressLint("MissingPermission")
    private void requestUpdatesFromProviders() {
        if (!isUpdateRequested) {
            // request location updates from the providers
            for (String providerName : PROVIDERS) {
                if (isProviderAvailable(providerName)) {
                    // Do not provide minUpdateDistance here, so we can filter by accuracy
                    locationManager.requestLocationUpdates(providerName, minUpdateTime, 0, this);
                    boolean isEnabled = locationManager.isProviderEnabled(providerName);
                    if (isEnabled) {
                        enabledProviders.add(providerName);
                    }
                    Log.v(TAG,
                            String.format("Subscribed to updates from location provider %s enabled: %b", providerName, isEnabled));
                } else {
                    Log.v(TAG,
                            String.format("The location provider %s is not available.", providerName));
                }
            }
            isUpdateRequested = true;
        }
    }

    /**
     * Stops updates from the providers
     */
    private void stopUpdatesFromProviders() {
        isUpdateRequested = false;
        // disable updates because we have no more users
        locationManager.removeUpdates(this);
        enabledProviders.clear();
    }

    /**
     * Handles a new location and notifies the LocationUser if appropriate
     *
     * @param location The new location.
     */
    private void handleNewLocation(Location location) {
        Log.v(TAG, String.format("New location %s", location));
        for (LocationSubscription locationSubscription : locationSubscriptions) {
            locationSubscription.handleNewLocation(location);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Nothing to do here, method deprecated
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (Arrays.asList(PROVIDERS).contains(provider) && !enabledProviders.contains(provider)) {
            Log.i(TAG, "A location provider has been enabled: " + provider);
            enabledProviders.add(provider);
            // also handle location from this provider
            @SuppressLint("MissingPermission") Location l = locationManager.getLastKnownLocation(provider);
            if (l != null) {
                handleNewLocation(l);
            }
            // Notify LocationUsers that a new provider was enabled
            for (LocationSubscription subscription : locationSubscriptions) {
                subscription.onProviderEnabled(provider);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "A location provider has been disabled: " + provider);
        if (enabledProviders.remove(provider) && enabledProviders.isEmpty()) {
            Log.i(TAG, "All providers have been disabled!");
            for (LocationSubscription subscription : locationSubscriptions) {
                subscription.onAllProvidersDisabled();
            }
        }
    }

    /**
     * Checks if the given provider is available or not
     *
     * @param providerName - Name of the provider
     * @return true if it's available, else false
     */
    private boolean isProviderAvailable(String providerName) {
        return locationManager.getAllProviders().contains(providerName);
    }

    /**
     * Returns the Last Known Location
     *
     * @return Location
     */




    public static class DoubleSubscriptionException extends RuntimeException {
    }
}

