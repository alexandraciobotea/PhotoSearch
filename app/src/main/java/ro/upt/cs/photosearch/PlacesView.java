package ro.upt.cs.photosearch;



import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ro.upt.cs.photosearch.entities.Azimuth;
import ro.upt.cs.photosearch.entities.Place;

/**
 * Used to display objects in front of the user
 */
public class PlacesView extends FrameLayout implements AzimuthChangedListener {
    /**
     * Log tag used for this view
     */
    private static final String TAG = "PlacesView";

    /**
     * Represents the user(device) horizontal view limit
     */
    private MyViewLimit viewLimit;

    /**
     * The places views that are currently available
     */
    private ArrayList<PlaceDescriptionView> placeDescriptionViews = new ArrayList<>();


    /**
     * @inheritDoc
     */
    @SuppressWarnings("unused")
    public PlacesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        prepareView();
    }

    /**
     * @inheritDoc
     */
    @SuppressWarnings("unused")
    public PlacesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepareView();
    }

    /**
     * @inheritDoc
     */
    @SuppressWarnings("unused")
    public PlacesView(Context context) {
        super(context);
        prepareView();
    }

    /**
     * Contains some initial configurations for the view
     */
    private void prepareView() {
        viewLimit = new MyViewLimit(new Azimuth(0d), 45, getWidth());

        // add dummy stuff
        // TODO remove this or move elsewhere
        placeDescriptionViews.add(new PlaceDescriptionView(getContext(), new PlaceObject.DummyPlaceObject("N", 0)));
        placeDescriptionViews.add(new PlaceDescriptionView(getContext(), new PlaceObject.DummyPlaceObject("S", 180)));
        placeDescriptionViews.add(new PlaceDescriptionView(getContext(), new PlaceObject.DummyPlaceObject("E", 90)));
        placeDescriptionViews.add(new PlaceDescriptionView(getContext(), new PlaceObject.DummyPlaceObject("W", 270)));
    }

    @Override
    public void onAzimuthChanged(Azimuth azimuth) {
        viewLimit.setFrontAngle(azimuth);
        // Update the UI for the new angle
        updateView();
        Log.d(TAG,"In Azimuth changed in placesView");
    }

    /**
     * Happens when we have a new set of places.
     */
    public void setPlaces(Iterable<PlaceObject> places) {
        Log.i(TAG, "PlacesView::setPlaces() called!");
        int i = 0;

        // Populate the list
        for (PlaceObject placeObject : places) {
            setOrAddDescriptionViewForIndex(i, placeObject);
            i++;
        }

        // remove the not needed views
        while (placeDescriptionViews.size() > i) {
            placeDescriptionViews.remove(placeDescriptionViews.size() - 1);
        }

        // Method used to invoke UI update
        updateView();
    }

    /**
     * Set the place description view at the given index in the list with the specified place object.
     * If the view does not exist, it will be created.
     */
    private void setOrAddDescriptionViewForIndex(int index, PlaceObject placeObject) {
        if (placeDescriptionViews.size() > index) {
            PlaceDescriptionView view = placeDescriptionViews.get(index);
            view.setPlaceObject(placeObject);
        } else {
            PlaceDescriptionView view = new PlaceDescriptionView(this.getContext(), placeObject);
            placeDescriptionViews.add(index, view);
        }
    }

    /**
     * Used to invoke UI update
     */
    private void updateView() {
        // sort the views by distance
        Collections.sort(placeDescriptionViews, new Comparator<PlaceDescriptionView>() {
            @Override
            public int compare(PlaceDescriptionView lhs, PlaceDescriptionView rhs) {

                Azimuth currentAzmituh = viewLimit.getFrontAngle();

                // compare distance from ploygons to the current position
                return Double.compare(lhs.getPolygonInfo().getDistance(), rhs.getPolygonInfo().getDistance());
            }
        });

        refreshVisibleViews();
        // Redraw UI
        invalidate();
    }

    /**
     * Refreshes the visible views (the list child objects of this view)
     */
    private int nr;
    private void refreshVisibleViews() {
        // remove all child views
        removeAllViews();
        // counter for places
            nr =0;
            // auxiliary list retaining visible places (close places and in the right direction)
            ArrayList<PlaceDescriptionView> aux = new ArrayList<PlaceDescriptionView>();

            for (PlaceDescriptionView view : placeDescriptionViews) {

                PolarPolygonInfo polygonInfo = view.getPolygonInfo();
                // conditions met, places are added to the auxiliary list
                if (!polygonInfo.isEmpty() && checkPlaceVisibility(view)) {
                    aux.add(view);

                }
            }
            // sort auxiliary list based on the leftest place
            Collections.sort(aux, new Comparator<PlaceDescriptionView>() {

                @Override
                public int compare(PlaceDescriptionView o1, PlaceDescriptionView o2) {
                    return Integer.compare(o1.getDimensionLeft(),o2.getDimensionLeft());
                }
            });
            for(PlaceDescriptionView view:aux) {
                // the displaying of the obtained places from the auxiliary list is calculated to fit in the layout
                view.setDimensions(getWidth()/nr, aux.indexOf(view)*getWidth()/nr, getHeight());
                addView(view);
            }
    }
    /**
     * Checks if the place is on user azimuth
     */
    private boolean checkPlaceVisibility(PlaceDescriptionView placeDescriptionView) {
        PolarPolygonInfo polygonInfo = placeDescriptionView.getPolygonInfo();
        MyViewLimit.ObjectDrawInfo drawInfo = viewLimit.getDrawInfo(polygonInfo.getMinAzimuth(), polygonInfo.getMaxAzimuth());
        if (drawInfo.isInsideView && nr<3 && adjustDrawInfoAndCheckIfItFits(drawInfo)) { // if inside view (intersection with a larger pair of azimuth values)
                                                                                         // if 3 places are not yet obtained
            nr++; //incrementez if conditions are met
            placeDescriptionView.setDimensions(drawInfo.width, drawInfo.left, getHeight());
            return true;
//            Log.d(TAG, String.format("View for place %s will be from %d px to %d px. (width:%d), dist=%.0f",
//                    placeDescriptionView.getText(), drawInfo.left, drawInfo.width + drawInfo.left, drawInfo.width, placeDescriptionView.getPolygonInfo().getDistance()));
//            addView(placeDescriptionView);
        }
        return false;
    }

    /**
     * Adjusts the given draw info to fit on the screen.
     * This will check all already added views, and compare their sizes
     *
     * @return Boolean which tells us if the draw info can be drawn to the screen or not
     */
    private boolean adjustDrawInfoAndCheckIfItFits(MyViewLimit.ObjectDrawInfo drawInfo) {
        int drawLeft = drawInfo.left;
        int drawRight = drawInfo.left + drawInfo.width;
        for (int i = 0; i < getChildCount(); i++) {
            PlaceDescriptionView view = (PlaceDescriptionView) getChildAt(i);
            int left = view.getDimensionLeft();
            int width = view.getDimensionWidth();
            int right = left + width;
            if (drawLeft >= left && drawLeft <= right) {
                drawLeft = right;
            }
            if (drawRight >= left && drawRight <= right) {
                drawRight = left;
            }
            if (drawLeft <= left && drawRight >= right) {
                // completely inside
                // TODO do a more correct handling of this situation because it can make the places flicker while you rotate this way...
                drawLeft = 0;
                drawRight = 0;
            }
        }
        drawInfo.left = drawLeft;
        drawInfo.width = drawRight - drawLeft;
        return drawInfo.width > 0;
    }

    /**
     * @return null if no place is in view
     */
    public Place getRepresentativeViewedPlace() {
        if (getChildCount() == 0) {
            return null;
        } else {
            return ((PlaceDescriptionView) getChildAt(0)).getPlaceObject().getPlace();
        }
    }

    /**
     * Happens when the view's size changes.
     */
    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        Log.i(TAG, String.format("View size changed to %dx%d", newWidth, newHeight));
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
        viewLimit.setDisplayWidth(newWidth);
    }

    /**
     * Public setter for HorizontalViewAngle
     *
     * @param horizontalViewAngle int
     */
    public void setHorizontalViewAngle(int horizontalViewAngle) {
        Log.i(TAG, String.format("The view angle is now changed to %d", horizontalViewAngle));
        viewLimit.setViewAngleWidth(horizontalViewAngle);
    }
}
