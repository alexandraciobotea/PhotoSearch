package ro.upt.cs.photosearch;


import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class PlaceDescriptionView extends AppCompatTextView {
    /**
     * PlaceView Tag used for Logging in PlaceDescriptionView
     */
    private static final String TAG = "PlaceDescriptionView";

    /**
     * Represents the View opacity value
     * The view will be 50 % transparent
     */
    private static final int BACKGROUND_OPACITY = 75;

    /**
     * Defines the view corner radius
     */
    private static final int CORNER_RADIUS = 5;

    /**
     * Define marquee repeat count to infinity
     */
    private static final int MARQUEE_REPEAT_FOREVER = -1;

    /**
     * This is used to get the background colors
     */
    private static final SimpleColorFactory colorFactory = new SimpleColorFactory();

    /**
     * The object that is represented by this view on the UI
     */
    private PlaceObject placeObject;

    private int left = 0;
    private int width = 0;

    /**
     * Holds information about the visible region
     */
    private PolarPolygonInfo visibleRegionInfo;
    private GradientDrawable background;

    public PlaceDescriptionView(Context context, PlaceObject placeObject) {
        super(context);
        initDrawParameters();
        setPlaceObject(placeObject);
    }

    /**
     * @inheritDoc
     */
    @SuppressWarnings("unused")
    public PlaceDescriptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDrawParameters();
    }

    /**
     * @inheritDoc
     */
    @SuppressWarnings("unused")
    public PlaceDescriptionView(Context context) {
        super(context);
        initDrawParameters();
    }
    /**
     * Called every time a view of this type is created
     * This will initialise the view to a standard outlook
     */
    private void initDrawParameters() {
        // Set View Background & Rounded Corner
        background = new GradientDrawable();
        // Set View Opacity
        background.setAlpha(BACKGROUND_OPACITY);
        // Define Corner Radius and set it to be place_icon
        background.setCornerRadius(CORNER_RADIUS);
        setBackground(background);
        // Set Custom TypeFace for View
        setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf"));
        // Set Text Color to blue
        setTextColor(Color.BLACK);
        // Set Text Size
        setTextSize(7);

        // Set Text Repeat Forever
        setSingleLine(false);
        setEllipsize(TextUtils.TruncateAt.END);
        setHorizontallyScrolling(true);
       // setLines(3);
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
        //setLineHeight(15);
        setHorizontallyScrolling(false);
        setVerticalScrollBarEnabled(true);
        setMaxLines(100);
        setEllipsize(null);

        //setMarqueeRepeatLimit(MARQUEE_REPEAT_FOREVER);
        setLayoutParams(new FrameLayout.LayoutParams(10, 10));

    }

    /**
     * Get information about the currently visible region
     */
    public PolarPolygonInfo getPolygonInfo() {
        return visibleRegionInfo;
    }

    public PlaceObject getPlaceObject() {
        return placeObject;
    }

    /**
     * Set the placeObject which is displayed by this view.
     */
    public void setPlaceObject(PlaceObject placeObject) {
        this.placeObject = placeObject;
        setText(placeObject.getName());
        PolarPolygon visibleRegion = placeObject.getPolygon();
        setVisibleRegion(visibleRegion);
        // set the color based on the name of the place.
        // this way a place will always receive the same color.

        int color = colorFactory.getColorForIndex(placeObject.getName().hashCode());
        background.setColor(color);
    }

    /**
     * Set the visible region of the object
     */
    private void setVisibleRegion(PolarPolygon visibleRegion) {
        visibleRegionInfo = PolarPolygonInfo.forPolygon(visibleRegion);
    }

    /**
     * Set the dimensions and the position of the view
     *
     * @param width The width in pixels
     * @param left  The left coordinate (starting point) in pixels
     */
    public void setDimensions(int width, int left, int height) {
        this.width = width;
        this.left = left;
        // set width and height using layout params
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = width;
        lp.height = height;
        setLayoutParams(lp);
        // set the X position using this nice method
        setX(left);
        setWidth(width);
    }

    public int getDimensionLeft() {
        return left;
    }

    public int getDimensionWidth() {
        return width;
    }
}
