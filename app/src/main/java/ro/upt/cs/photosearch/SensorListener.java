package ro.upt.cs.photosearch;

import android.location.Location;

import ro.upt.cs.photosearch.entities.Azimuth;

public interface SensorListener {

    void onAzimuthChanged(Azimuth direction);

    void onNewLocation(Location location);
}
