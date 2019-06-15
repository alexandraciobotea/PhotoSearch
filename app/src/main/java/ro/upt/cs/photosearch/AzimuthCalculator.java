package ro.upt.cs.photosearch;


import android.util.Log;

import java.util.ArrayList;

import ro.upt.cs.photosearch.entities.Azimuth;


public abstract class AzimuthCalculator {

    private static final String TAG = "AzimuthCalculator";

    /**
     * List of listeners who should be notified on azimuth events
     */
    private ArrayList<AzimuthChangedListener> listOfListeners = new ArrayList<>();

    private Azimuth lastNotifiedAzimuth = null;

    /**
     * Adds a listener to the list of listeners
     *
     * @param listener AzimuthChangedListener
     */
    public void addListener(AzimuthChangedListener listener) {
        listOfListeners.add(listener);
        Log.i(TAG, "AzimuthCalculator::addListener() - Obj: " + listener.hashCode() + " registered!");
        if (lastNotifiedAzimuth != null) {
            // initial notification
            listener.onAzimuthChanged(lastNotifiedAzimuth);
        }
    }

    /**
     * Removes the listener from the list of listeners
     *
     * @param listener AzimuthChangedListener
     * @return true on success, false if the listener is no part of the list
     */
    public boolean removeListener(AzimuthChangedListener listener) {
        boolean removed = listOfListeners.remove(listener);
        Log.i(TAG, "AzimuthCalculator::removeListener() - Obj: " + listener.hashCode() + " removed: " + removed);
        return removed;
    }

    /**
     * Notify all the listeners about the new azimuth value
     *
     * @param azimuth Azimuth
     */
    protected void notifyAll(Azimuth azimuth) {
        lastNotifiedAzimuth = azimuth;
        for (AzimuthChangedListener listener : listOfListeners) {
            listener.onAzimuthChanged(azimuth);
        }
    }

    public Azimuth getLastNotifiedAzimuth() {
        return lastNotifiedAzimuth;
    }

    public abstract void start();

    public abstract void stop();

    public abstract void setNotificationDelta(int delta);

    // TODO use androidannotaions prefs and get rid of this method
    public abstract void setFilterMemorySize(int size);
}
