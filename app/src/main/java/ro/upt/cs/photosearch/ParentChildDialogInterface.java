package ro.upt.cs.photosearch;



import android.location.Location;

import ro.upt.cs.photosearch.entities.WaitingForGPSMessageListener;


/**
 * Contains methods that parent activity must implement and offer
 * to child fragments to ensure basic communication methods
 */
public interface ParentChildDialogInterface {

    /**
     * Method called by fragments to invoke a notification from the parent activity
     *
     * @param text Error Description
     */
    void onError(String text);

    /**
     * Used to subscribe fragment for Sensor event
     *
     * @param listener SensorListener
     */
   // void subscribeForSensorEvents(SensorListener listener);

    /**
     * Used to remove listener from Sensor event notifications
     *
     * @param listener SensorListener
     */
    void unsubscribeFromSensorEvents(SensorListener listener);

    /**
     * Returns the last known location from the system
     *
     * @return Location
     */
    Location getLastKnownLocation();

    //void subscribeForCameraChanges(CameraStatusChangedListener listener);

    void disableWaitingForLocationMessage();

    void enableWaitingForLocationMessage();

    void subscribeForWaitingForGPSEvents(WaitingForGPSMessageListener listener);
}
