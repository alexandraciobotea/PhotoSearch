package ro.upt.cs.photosearch;

import android.Manifest;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ro.upt.cs.photosearch.data.MyData;
import ro.upt.cs.photosearch.entities.Azimuth;
import ro.upt.cs.photosearch.entities.LocationUser;
import ro.upt.cs.photosearch.entities.Place;

import static android.content.Context.SENSOR_SERVICE;

import cn.pedant.SweetAlert.SweetAlertDialog;
public class CameraFragment extends Fragment implements LocationUser, AzimuthChangedListener,SensorListener, SensorEventListener {


    private final String TAG = "CAMERA_FRAGMENT";



    private CameraViewModel mViewModel;
    private Context context;
    protected LocationMgr locationMgr;
    private SensorManager sensorManager;
    private LocationSubscription locationSubscription = null;
    private boolean notifiedThisSession = false;
    private String projectToken = "cc5c92b3eabaa9bbd7ed275a338cd5b7";
    // camera2 where null first
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraSession;

    //camera2
    Size imageDimension;
    private TextureView textureView;
    private boolean camera_loading = true;
    Handler backgroundHandler;
    HandlerThread handlerThread;
    private boolean shouldShowWaitingForGpsMessage;
    private boolean isWaitingForGpsMessageAllowed;
    private boolean isWaitingForGpsMessageShown;
    private Toast waitinForGpsMessage;
    private MixpanelAPI mixpanel;

    private ImageButton photo_button;
    protected SearchResult lastSearchResult = null;
    private Location lastReloadLocation = null;



    private boolean isSubscribedToEvents = false;
    private ParentChildDialogInterface parentInterface = null;
    protected PlacesView placesView;

    private  Sensor accelerometer;
    private  Sensor magnetometer;
    private  SensorManager mSensorManager;

    private Azimuth prev_azimuth = new Azimuth(0d);

    private   float[] mAccelerometer = null;
    private   float[] mGeomagnetic = null;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    private CaptureRequest captureRequest;
    private TextureView.SurfaceTextureListener surfaceTextureLister; //surfTextureListener implementat din interfata surfacetextlistener care e in textureview class
    private File galleryFolder = null;
    private ProgressBar mProgress;


    public static CameraFragment newInstance() {
        return new CameraFragment();
    }


    @Override
    public void onAttach(Context context) {
        parentInterface = (ParentChildDialogInterface) context;
        super.onAttach(context);
        this.context=context;
        }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_fragment, container, false);
        placesView =  (PlacesView) view.findViewById(R.id.placesView);
        textureView = view.findViewById(R.id.textureView);
       // mProgress = new ProgressBar(this.getContext());
        assert textureView != null;
        surfaceTextureLister = new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                openCamera();
            }

            // called when the size of the screen suffers modifications
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                // recalculate the new sizes if the screen is rotated
                configureTransform(width, height);
            }
            // returns true as the surface texture will get destroyed; nothing extra is done
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

            }
        };
        textureView.setSurfaceTextureListener(surfaceTextureLister);
        textureView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                lock();
                unlock();
                save_photo();


                return true;

            }

        });

        //display_coordinates = (TextView) view.findViewById(R.id.display_coordinates);
        photo_button = (ImageButton) view.findViewById(R.id.photo_button);


        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null && sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            Log.d(TAG, "sensor exists");
        }
        else{
            Log.d(TAG, "no sensor");
        }


        locationMgr = LocationMgr.getInstance(getContext());
        locationSubscription = new LocationSubscription(this);
        Log.d(TAG,"Fragment onCreateView");
        return view;
    }
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera){
            cameraDevice = camera;
            startCameraPreview();
        }

        @Override
        public void onDisconnected( @NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };
    private void lock() {
        try {
            cameraSession.capture(captureRequestBuilder.build(),
                    null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlock() {
        try {
            cameraSession.setRepeatingRequest(captureRequestBuilder.build(),
                    null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();

        }
    }
    private void createImageGallery() {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        galleryFolder = new File(storageDirectory, getResources().getString(R.string.app_name));


        if (!galleryFolder.exists()) {
            boolean wasCreated = galleryFolder.mkdirs();
            if (!wasCreated) {
                Log.e("CapturedImages", "Failed to create directory");
            }
        }
    }



    private File createImageFile(File galleryFolder) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "image_" + timeStamp + "_";
        return File.createTempFile(imageFileName, ".jpeg", galleryFolder);
    }

    private void startCameraPreview() {
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
        Surface surface = new Surface(surfaceTexture);

        try {
            // TEMPLATE_PREVIEW to create a simple capture request for  SurfaceView using the template designed for camera preview
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    //camera already closed
                    if (cameraDevice == null) {
                        return;
                    }
                    //when session ready, start displaying the preview
                    cameraSession = cameraCaptureSession;
                    updatePreview();

                    try {
                        captureRequest = captureRequestBuilder.build();
                        CameraFragment.this.cameraSession = cameraCaptureSession;
                        CameraFragment.this.cameraSession.setRepeatingRequest(captureRequest,
                                null, backgroundHandler);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    // Upload on Cloud

    private String doUploadTask(final Bitmap bitmap) {
        String path = null;
        try {
           // mProgress.setTitle("Uploading...");
           // mProgress.show();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            final String imageFileName = "image_" + timeStamp + "_";
            path = "images/" + imageFileName;

            // buffer to store data from stream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            // byte array with buffer content
            byte[] data_img = baos.toByteArray();
            final FirebaseStorage storage = FirebaseStorage.getInstance();
            final StorageReference storageReference = storage.getReference();
            final StorageReference reference = storageReference.child(path);
            final UploadTask uploadTask = reference.putBytes(data_img);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FirebaseStorage.getInstance().getReference().child("images/" + imageFileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.i(TAG, "Got url, update DB");
                            //save image's information on Firestore
                            MyPhoto p = new MyPhoto();
                            p.latitude = lastReloadLocation.getLatitude();
                            p.longitude = lastReloadLocation.getLongitude();
                            p.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            p.url = uri.toString();
                            FirebaseFirestore.getInstance().collection("photos").add(p);
                           // mProgress.dismiss();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Image wasn't saved. Exception: " + e.getMessage());
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_LONG).show();
                   // mProgress.dismiss();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Still here");
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                    Toast.makeText(context,"Uploaded", Toast.LENGTH_SHORT).show();
                   //mProgress.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception at getting the bitmap from camera or other");
            e.printStackTrace();
            Toast.makeText(context, "Image not taken. Try again!", Toast.LENGTH_LONG).show();
        } finally {
            return path;
        }

    }


    public void save_photo(){
        final AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(CameraFragment.this.getContext());
        myAlertDialog.setTitle("Save Photo");
        myAlertDialog.setMessage("Where do you want to save photo?");


        myAlertDialog.setNeutralButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        myAlertDialog.setNegativeButton("Cloud", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doUploadTask(textureView.getBitmap());
            }
        });
        myAlertDialog.setPositiveButton("Device",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        FileOutputStream outputPhoto = null;

                        try {
                            if(galleryFolder == null) createImageGallery();
                            // create a file into folder where outputPhoto is saved after Bitmap compression
                            outputPhoto = new FileOutputStream(createImageFile(galleryFolder));
                            textureView.getBitmap()
                                    .compress(Bitmap.CompressFormat.JPEG, 100, outputPhoto);

                            Log.d(TAG,"Photo has been taken");
                            Toast.makeText(CameraFragment.this.getContext(),"Photo has been saved on the device",Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            //unlock();
                            try {
                                if (outputPhoto != null) {
                                    outputPhoto.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        myAlertDialog.show();


    }


    private void updatePreview() {
        if (cameraDevice == null)
            return;
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    int sensorOrientation;
    private void openCamera() {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraCaptureIds = manager.getCameraIdList();
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraCaptureIds[0]);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            if (characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!=null) {
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            }
            configureTransform(imageDimension.getWidth(), imageDimension.getHeight());




            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(cameraCaptureIds[0], stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (cameraSession != null){
            cameraSession.close();
            cameraSession = null;
        }
//        if (null != imageReader) {
//            imageReader.close();
//            imageReader = null;
//        }
    }
    /**
     * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        FragmentActivity activity = getActivity();
        if (null == textureView || null == imageDimension || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, imageDimension.getHeight(), imageDimension.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / imageDimension.getHeight(),
                    (float) viewWidth / imageDimension.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Log.d(TAG,"Fragment created");
        mSensorManager = (SensorManager)getContext().getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);



    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);
        camera_loading = true;
        Log.d(TAG,"Fragment onActivity created");

        //obtinere coordonate daca exista
        if(mViewModel.getLocation() != null) {
           // display_coordinates.setText(mViewModel.getLocation().getLatitude() + "-" + mViewModel.getLocation().getLongitude());

        }
        ArrayList <Place> newPlaces = new ArrayList <Place>();

    }


    @Override
    public void onResume() {
        super.onResume();
        subscribeToLocationUpdates();
        Log.d(TAG, "Activity onResume called.");
        mSensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this,magnetometer,SensorManager.SENSOR_DELAY_UI);
        //camera2
        startBackgroundThread();
        //enable here if camra doesn't work
        if(textureView.isAvailable()){
            openCamera();
        }
        else{
            textureView.setSurfaceTextureListener(surfaceTextureLister);
        }

    }
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"Fragment started");
    }
    public void onNewLocation(Location location) {
        Log.d(TAG,"Location obtained");
        //display_coordinates.setText(location.getLatitude()+" - " + location.getLongitude());
        lastReloadLocation = location;
        loadPlaces(location);
        if(mViewModel!=null) {
            mViewModel.setLocation(location);
        }
    }

    public void onAzimuthChanged(Azimuth direction) {
//        // This fragment don't cares what direction contains
//        // Just forward azimuth to placesView object
//        if (placesView != null) {
//            placesView.onAzimuthChanged(direction);
//        }
//        Log.d(TAG, "In onAzimuthChanged");
    }


    public void loadPlaces(Location location) {
        WikimapiaClient client = new WikimapiaClient(MyData.WIKIMAPIA_API_KEY);
        Log.d(TAG, "In Loading places");
        Log.d(TAG,"lat" +location.getLatitude() +"long" + location.getLongitude());
        client.getNearest(location.getLongitude(),location.getLatitude())
                .setCount(50)
                .execute(new Callback<SearchResult>() {
                    public void onResponse(Call<SearchResult> call,
                                           Response<SearchResult> response) {
                        Log.d(TAG,"Here in onResponse");
                        if(response.isSuccessful()) {
                            List<Place> places = response.body().getPlaces();
                            Log.d(TAG, response.raw().request().url().toString());
                            lastSearchResult = response.body();
                            updatePlaces(lastReloadLocation);
                            if (!places.isEmpty()) {
                                Log.d(TAG, "in success");
                                for (int i = 0; i < places.size(); i++) {
                                    Log.d(TAG, "Places are:" + places.get(i).getTitle());
                                }

                            }
                        }

                    }

                    @Override
                    public void onFailure(Call<SearchResult> call, Throwable t) {
                        Toast.makeText(getActivity(), "Callback failed!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG,"Failure for loading places");
                    }

                });

    }

    private void updatePlaces(Location location) {
        if (lastSearchResult != null && location != null) {
            List<PlaceObject> places = new ArrayList<>(lastSearchResult.getPlaces().size());
            double lon = location.getLongitude();
            double lat = location.getLatitude();
            for (Place p : lastSearchResult.getPlaces()) {
                PlaceObject po = new PlaceObject(p, lon, lat);
                places.add(po);
            }
            placesView.setPlaces(places);

        }
    }


    @Override
    public void onStop() {
        super.onStop();
        closeCamera();
        Log.d(TAG,"Fragment stopped");
        locationMgr.unsubscribe(locationSubscription);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"Fragment destroyed");
        locationSubscription = null;
    }
    protected void subscribeToLocationUpdates() {

        locationMgr.setMinUpdateTime(15);

        locationSubscription.setMaxUpdateTime(5);

        locationSubscription.setMinRequiredAccuracy(20);

        locationSubscription.setMinUpdateDistance(10);
        // Register location mgr for location events
        locationMgr.subscribeToProviders(locationSubscription);
        Log.d(TAG,"In subscribe to updates");

    }
    //camera2
    private void startBackgroundThread(){
        handlerThread = new HandlerThread("Camera background thread");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void onPause() {
        super.onPause();
        locationMgr.unsubscribe(locationSubscription);
        mSensorManager.unregisterListener(this,accelerometer);
        mSensorManager.unregisterListener(this,magnetometer);
        Log.d(TAG, "CameraFragment::onPause() called.");
        try {
            stopBackgroundThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void stopBackgroundThread() throws InterruptedException{
        if (backgroundHandler != null) {
            handlerThread.quitSafely();
            handlerThread = null;
            backgroundHandler = null;
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
        waitinForGpsMessage = Toast.makeText(getContext(), "Wainting for GPS...", Toast.LENGTH_SHORT);
    }
    @Override

    public void onProviderEnabled(String provider) {

        notifiedThisSession = false;
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
    @Override
    public void onAllProvidersDisabled() {
        Log.d(TAG, "in on all providers disabled");
        shouldShowWaitingForGpsMessage = true;
        updateWaitingForGpsMessage();
        if (!notifiedThisSession) {
            notifiedThisSession = true;
            Log.d(TAG,"Provider disabled");
            new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
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
        Log.d(TAG, "in not acc loc");
        shouldShowWaitingForGpsMessage = true;
        updateWaitingForGpsMessage();
    }
    private void unsubscribeFromEvents() {
        if (isSubscribedToEvents) {
            // There is no more need for Sensor events, so remove registration
            parentInterface.unsubscribeFromSensorEvents(this);
            isSubscribedToEvents = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccelerometer = event.values;   // values retrieved by SensorEventListener of TYPE_ACCELEROMETER
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;     // values retrieved by SensorEventListener of TYPE_MAGNETIC_FIELD
        }
        if (mAccelerometer != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            float rotatedR[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mAccelerometer, mGeomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Y, rotatedR);
                 // obtains the array holding the three values (x,y,z)
                SensorManager.getOrientation(rotatedR, orientation);
                double value = orientation[0] ; // value of Azimuth
                float precision = 15f;
                Azimuth azimuth = new Azimuth((float) Math.toDegrees(value));

               if(azimuth.isAzimuthSignificantlyChanged(prev_azimuth,precision)){
                    if (placesView != null) {
                        placesView.onAzimuthChanged(azimuth);
                    }
                    prev_azimuth = azimuth;
                }

                Log.d(TAG, "AZIMUTH IS:" + azimuth.getValue());

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




}
