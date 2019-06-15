package ro.upt.cs.photosearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

//exact ca si RecyclerViewAdapter doar ca pt Google Marker
public class PopupAdapter implements GoogleMap.InfoWindowAdapter {
    private View popup=null;
    private LayoutInflater inflater=null;
    private HashMap<String, Uri> images=null;
    private Context ctxt=null;
    private int iconWidth=-1;
    private int iconHeight=-1;
    private Marker lastMarker=null;

    public PopupAdapter(Context ctxt, LayoutInflater inflater,
                        HashMap<String, Uri> images) {
        this.ctxt = ctxt;
        this.inflater = inflater;
        this.images = images;
        iconWidth = ctxt.getResources().getDimensionPixelSize(R.dimen.marker_icon_width);
        iconHeight = ctxt.getResources().getDimensionPixelSize(R.dimen.marker_icon_height);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return(null);
    }


    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {
        if (popup == null) {
            popup=inflater.inflate(R.layout.popup_marker, null);
        }

        if (lastMarker == null
                || !lastMarker.getId().equals(marker.getId())) {
            lastMarker=marker;

            TextView tv=(TextView)popup.findViewById(R.id.marker_title);

            tv.setText(marker.getTitle());


        }

        return(popup);
    }


}