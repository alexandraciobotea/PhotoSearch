package ro.upt.cs.photosearch;

import ro.upt.cs.photosearch.entities.Azimuth;

/**
 * Listener interface for azimuth and polarAzimuth change
 */
public interface AzimuthChangedListener {

    /**
     * Called when new azimuth direction is available and
     * the UI must be updated conform new values
     *
     * @param direction Azimuth Value
     */
    public void onAzimuthChanged(Azimuth direction);
}
