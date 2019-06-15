package ro.upt.cs.photosearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ro.upt.cs.photosearch.usecases.LoginActivity;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private FirebaseFirestore database;
    private FirebaseAuth auth;
    private String uid;
    private FirebaseAuth.AuthStateListener authListener;
    private ArrayList<MyPhoto> photoList = new ArrayList<MyPhoto>();
    private String TAG="MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        else {
            try {
                uid = auth.getCurrentUser().getUid();
            }
            catch (Exception e) {
                e.printStackTrace();
                this.onBackPressed();
                finish();
            }
        }
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        FirebaseFirestore.getInstance().collection("photos").whereEqualTo("uid",FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    List<DocumentSnapshot> photos = task.getResult().getDocuments();
                    if(photos.size() == 0) {
                        Toast.makeText(MapsActivity.this, "No photos in cloud", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    photoList.clear();
                    for(DocumentSnapshot doc : photos) {
                        MyPhoto p = doc.toObject(MyPhoto.class);
                        p.cloud = true;
                        photoList.add(p);
                    }
                    addItems();
                }
                else {
                    Log.e(TAG,task.getException().toString());
                }
            }
        });
    }
    private void addItems() {
        final HashMap<String,Uri> markerImgUri = new HashMap<String, Uri>();
       // Marker posMarker = null;
       //final View mCustomMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker, null);
        mMap.setInfoWindowAdapter(new PopupAdapter(MapsActivity.this,getLayoutInflater(),markerImgUri));
        mMap.setOnInfoWindowClickListener(this);
        for(int i=0;i<photoList.size();i++) {
                final LatLng item = new LatLng(Double.valueOf(photoList.get(i).latitude), Double.valueOf(photoList.get(i).longitude));
                final MyPhoto p = photoList.get(i);
                final int index = i;

                //important
                final View markerView = getLayoutInflater().inflate(R.layout.custom_marker,null);
                final ImageView markerIcon = (ImageView) markerView.findViewById(R.id.profile_image_marker);
                Picasso.with(getApplicationContext()).load(Uri.parse(photoList.get(i).url))
                        .resize(40,40)
                        .error(R.drawable.error_circle)
                        .placeholder(R.mipmap.ic_launcher_round)
                        .into(markerIcon, new Callback() {
                            @Override
                            public void onSuccess() {
                                final Marker m = mMap.addMarker(new MarkerOptions().position(item));
                                m.setTag(p);
                                m.setTitle(getLocationName(Double.valueOf(p.latitude), Double.valueOf(p.longitude)));
                                markerImgUri.put(m.getId(),Uri.parse(p.url));
                                m.setIcon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(MapsActivity.this,markerView)));

                            }

                            @Override
                            public void onError() {
                                final Marker m = mMap.addMarker(new MarkerOptions().position(item));
                                m.setTag(p);
                                m.setTitle(getLocationName(Double.valueOf(p.latitude), Double.valueOf(p.longitude)));
                                markerImgUri.put(m.getId(),Uri.parse(p.url));
                                m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));

                            }
                        });

        }

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        marker.hideInfoWindow();
    }

    private String getLocationName(Double lat,Double lon) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            if(lon != null && lat!=null) {
                addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                return addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
            }
            else {
                return "No location";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "No location";
        }
    }

    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }
    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
