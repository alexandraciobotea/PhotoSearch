package ro.upt.cs.photosearch;

import android.arch.lifecycle.ViewModel;
import android.location.Location;

import java.lang.String;
public class CameraViewModel extends ViewModel {
    private static final String TAG = "CameraViewModel";
    // TODO: Implement the ViewModel
    private boolean previewNeeded = true;
    private Location location = null;


    public boolean getPreviewNeeded() {
        return previewNeeded;
    }

    public void setPreviewNeeded(boolean previewNeeded) {
        this.previewNeeded = previewNeeded;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}