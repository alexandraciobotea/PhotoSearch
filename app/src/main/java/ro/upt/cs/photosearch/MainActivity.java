package ro.upt.cs.photosearch;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


import cn.pedant.SweetAlert.SweetAlertDialog;
import ro.upt.cs.photosearch.entities.Azimuth;
import ro.upt.cs.photosearch.entities.LocationUser;
import ro.upt.cs.photosearch.entities.WaitingForGPSMessageListener;
import ro.upt.cs.photosearch.usecases.LoginActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, LocationUser,ParentChildDialogInterface {

    private static final String TAG = "MAIN_ACTIVITY";
    protected LocationMgr locationMgr;
    private LocationSubscription locationSubscription = null;
    private boolean isWaitingForGpsMessageAllowed = true;
    private boolean isWaitingForGpsMessageShown = false;
    private boolean shouldShowWaitingForGpsMessage = false;
    private WaitingForGPSMessageListener waitingForGPSMessageListener = null;
    private List<SensorListener> listeners = null;
    private Toast waitinForGpsMessage;
    private String projectToken = "cc5c92b3eabaa9bbd7ed275a338cd5b7";
    private MixpanelAPI mixpanel;




    private ArrayList<AzimuthChangedListener> listOfListeners = new ArrayList<>();





    private AzimuthCalculator azimuthCalculator = null;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link FragmentPagerAdapter} derivative, which will keep every loaded
     * fragment in memory. If this becomes too memory intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private FragmentPagerAdapter mFragmentPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    /**
     * Whether or not the system UI should be auto-hidden after {@link #AUTO_HIDE_DELAY_MILLIS}
     * milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction
     * before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates and a change of the status
     * and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 1000;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    final static int PERMISSION_ACCESS_COARSE_LOCATION = 1;
    final static int PERMISSION_ACCESS_FINE_LOCATION = 2;
    final static int PERMISSION_WRITE_EXTERNAL_STORAGE = 3;
    final static int PERMISSION_ACCESS_CAMERA = 4;
    private boolean notifiedThisSession = false;
    private Context context = this;
    private FirebaseAuth auth;
    private String uid;
    private FirebaseAuth.AuthStateListener authListener;
    private Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                super.onBackPressed();
                finish();
            }
        }
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else {

                }
            }
        };
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        locationMgr = LocationMgr.getInstance(this);
        locationSubscription = new LocationSubscription(this);
        // SensorListener related declarations
        listeners = new ArrayList<>();
        //azimuthCalculator = ;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mFragmentPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mFragmentPagerAdapter);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(this);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mixpanel = MixpanelAPI.getInstance(this, projectToken);
        try {
            JSONObject props = new JSONObject();
            props.put("Type", "AppOpen");
            mixpanel.track("Action", props);
        } catch (JSONException e) {
            Log.e("MYAPP", "Unable to add properties to JSONObject", e);
        }

        mVisible = true;

        mContentView = findViewById(R.id.fullscreen_content);

        if (Build.VERSION.SDK_INT >= 23) {
            if ( ContextCompat.checkSelfPermission( this, permission.ACCESS_FINE_LOCATION )
                != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this, new String[] {  permission.ACCESS_FINE_LOCATION  },
                    PERMISSION_ACCESS_FINE_LOCATION );
                return;
            }

            if ( ContextCompat.checkSelfPermission( this, permission.ACCESS_COARSE_LOCATION )
                != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this, new String[] {  permission.ACCESS_COARSE_LOCATION  },
                    PERMISSION_ACCESS_COARSE_LOCATION );
                return;
            }

            if ( ContextCompat.checkSelfPermission( this, permission.CAMERA )
                != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this, new String[] {  permission.CAMERA  },
                    PERMISSION_ACCESS_CAMERA );
                return;
            }

            if ( ContextCompat.checkSelfPermission( this, permission.WRITE_EXTERNAL_STORAGE )
                != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions( this, new String[] {  permission.WRITE_EXTERNAL_STORAGE  },
                    PERMISSION_WRITE_EXTERNAL_STORAGE );
                return;
            }
        }


    }

    private void signOut() {
        auth.signOut();
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
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(10000);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id == R.id.signout) {
            signOut();
        }
        else if(id == R.id.mapsLink) {
            Intent intent = new Intent(this,MapsActivity.class);
            startActivity(intent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onNewLocation(Location location) {
        Log.d(TAG,"Location longitude is: "+location.getLongitude());
        Log.d(TAG, "Location latitude is:"+location.getLatitude());
    }


    @Override
    public void onProviderEnabled(String provider) {
        notifiedThisSession = false;
    }

    @Override
    public void onAllProvidersDisabled() {
        shouldShowWaitingForGpsMessage = true;
        updateWaitingForGpsMessage();
        if (!notifiedThisSession) {
            notifiedThisSession = true;
            Log.d(TAG,"Provider disabled");
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getResources().getString(R.string.location_providers_disabled_message))
                    .setConfirmText(getResources().getString(R.string.sad_btn_check))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            mixpanel = MixpanelAPI.getInstance(context, projectToken);
                            try {
                                JSONObject props = new JSONObject();
                                props.put("Type", "GPSCheck");
                                mixpanel.track("Action", props);
                            } catch (JSONException e) {
                                Log.e("MYAPP", "Unable to add properties to JSONObject", e);
                            }
                        }
                    })
                    .showCancelButton(true)
                    .setCancelText(getString(R.string.btn_cancel_GPS))
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            mixpanel = MixpanelAPI.getInstance(context, projectToken);
                            try {
                                JSONObject props = new JSONObject();
                                props.put("Type", "GPSLater");
                                mixpanel.track("Action", props);
                            } catch (JSONException e) {
                                Log.e("MYAPP", "Unable to add properties to JSONObject", e);
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onNotAccurateNewLocation(Location location) {
        shouldShowWaitingForGpsMessage = true;
        updateWaitingForGpsMessage();
    }



    public void setAzimuth(){

    }

    public void setPoint(String name, double azimuth){


    }


    @Override
    public void onClick(View view) {
        if (view == mControlsView){
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
//            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
//            scanIntegrator.initiateScan();
        }
        else {
            toggle();
        }
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        /**
         * The fragment argument representing the section number for this fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(
                getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the
     * sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0)
                return new CameraFragment();
            else if (position == 1)
                return new RecycleViewFragment();

            return PlaceholderFragment.newInstance(position + 1);
        }


        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        subscribeToLocationUpdates();


    }

    @Override
    protected void onPause() {
        unsubscribeFromLocationUpdates();
        shouldShowWaitingForGpsMessage = false;
        super.onPause();
        updateWaitingForGpsMessage();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onError(String text) {
            Log.d(TAG, String.format("MainActivity::onError() - Showing error notification dialog with message %s.", text));
    }



    @Override
    public void unsubscribeFromSensorEvents(SensorListener listener) { listeners.remove(listener);

    }

    @Override
    public Location getLastKnownLocation() { return locationSubscription.getLastKnownLocation();

    }


    private void updateWaitingForGpsMessage() {
        if (shouldShowWaitingForGpsMessage && isWaitingForGpsMessageAllowed) {
            showWaitingForGpsMessage();
        } else {
            hideWaitingForGpsMessage();
        }
    }
    private void hideWaitingForGpsMessage() {
        if (isWaitingForGpsMessageShown) {
            waitinForGpsMessage.cancel();
            isWaitingForGpsMessageShown = false;
        }

    }
    private void showWaitingForGpsMessage() {
        if (!isWaitingForGpsMessageShown) {
            createWaitingForGpsMessage();
            waitinForGpsMessage.show();
            isWaitingForGpsMessageShown = true;
        }
    }

    private void createWaitingForGpsMessage() {
        waitinForGpsMessage = Toast.makeText(this, "Waiting for GPS...", Toast.LENGTH_SHORT);
    }

    //@Override
    public void enableWaitingForLocationMessage() {
        isWaitingForGpsMessageAllowed = true;
        updateWaitingForGpsMessage();
    }

   // @Override
    public void subscribeForWaitingForGPSEvents(WaitingForGPSMessageListener listener) {
        waitingForGPSMessageListener = listener;
    }

   // @Override
    public void disableWaitingForLocationMessage() {
        isWaitingForGpsMessageAllowed = false;
        updateWaitingForGpsMessage();
    }



    protected void subscribeToLocationUpdates() {

        locationMgr.setMinUpdateTime(15);

        locationSubscription.setMaxUpdateTime(5);

        locationSubscription.setMinRequiredAccuracy(20);

        locationSubscription.setMinUpdateDistance(10);
            // Register location mgr for location events
        locationMgr.subscribeToProviders(locationSubscription);

    }

    protected void unsubscribeFromLocationUpdates() {
        locationMgr.unsubscribe(locationSubscription);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

                return;
            }

            case PERMISSION_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }


                return;
            }

            case PERMISSION_ACCESS_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }


                return;
            }

            case PERMISSION_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }


                return;
            }
        }
    }
}
