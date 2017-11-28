/*
  Created by Karl on 26 Oct 2016.
 */
package ie.dit.dtw;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.location.Location;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;

public class Camera extends FragmentActivity {

    CameraManager manager;
    private CameraCaptureSession sesh;
    private CameraDevice device;
    private TextureView textureView;
    private Surface surface;
    private List<Surface> sList;
    CaptureRequest.Builder capReqB;
    private CaptureRequest capReq;
    private ImageReader imgReader;
    private Handler handler;
    HandlerThread handlerThread;
    private String cameraID;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private LocationGetter loc;

    double latitude;
    double longitude;
    Location cLocation;

    MyDBManager db;
    Cursor c;

    Image img;
    File file;
    ByteBuffer buffer;
    FileOutputStream output;
    byte[] bytes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);



        // In case this activity was started with special instructions from an
        // Intent, pass the Intent's extras to the fragment as arguments




        textureView = new TextureView(this);
        textureView = (TextureView) findViewById(R.id.pictuir);

        loc = new LocationGetter(this);
    }

    ImageReader.OnImageAvailableListener imgListen = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            img = reader.acquireNextImage();
            buffer = img.getPlanes()[0].getBuffer();
            bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            output = null;

            cLocation = loc.getLocation();
            if (cLocation != null) {
                longitude = cLocation.getLongitude();
                latitude = cLocation.getLatitude();
            } else {
                longitude = loc.getLongitude();
                latitude = loc.getLatitude();
            }
            loc.close();

            PicturedFragment newFragment = new PicturedFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            transaction.commit();

            if (device != null) {
                device.close();
                device = null;
            }
            if (sesh != null) {
                sesh.close();
                sesh = null;
            }
        }
    };

    public void saveImage(View view) {

        EditText et = (EditText) findViewById(R.id.picname);
        String name = et.getText().toString();
        Calendar cal = Calendar.getInstance();
        Random r = new Random();
        int i = r.nextInt(10000);

        db = new MyDBManager(this);

        try {
            db.open();
            db.insertPhoto(i + name + ".jpg", (float) latitude, (float) longitude, cal.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        file = new File(Camera.this.getFilesDir(), i + name + ".jpg");
        try {
            output = openFileOutput(i + name + ".jpg", Context.MODE_PRIVATE);
            output.write(bytes);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            img.close();
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        db.close();
        finish();
        //end reference
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (textureView.isAvailable()) {
            initializeCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
        if (img != null) {
            img.close();
        }
    }

    private final TextureView.SurfaceTextureListener textureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {

            initializeCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            try {
                onPause();
            }catch(Exception e){
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    CameraDevice.StateCallback devSCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            device = camera;
            setupCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
        }
    };

    CameraCaptureSession.CaptureCallback capCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {

        }
    };//end CameraCaptureSession

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.
        if (device != null) {
            device.close();
            device = null;
        }
        if (sesh != null) {
            sesh.close();
            sesh = null;
        }
        if (imgReader != null) {
            imgReader.close();
            imgReader = null;
        }
        if (loc != null) {
            loc.close();
        }

        Log.d("bug","paused");
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        // Create a new Fragment to be placed in the activity layout
        PreviewFragment firstFragment = new PreviewFragment();

        firstFragment.setArguments(getIntent().getExtras());

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, firstFragment).commit();

        // Get the Camera instance as the activity achieves full user focus
        if (textureView.isAvailable()) {
            initializeCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }

        if (loc == null) {
            loc = new LocationGetter(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }


    private void initializeCamera(int width, int height) {

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int sheight = displaymetrics.heightPixels;
        int swidth = displaymetrics.widthPixels;

        Matrix m = new Matrix();
        m.postScale((float) sheight / height, swidth / width);
        textureView.setTransform(m);

        SurfaceTexture texture = textureView.getSurfaceTexture();


        texture.setDefaultBufferSize(displaymetrics.widthPixels, displaymetrics.heightPixels);

        handlerThread = new HandlerThread("CameraBackground");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());


        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        imgReader = ImageReader.newInstance(size.x, size.y, ImageFormat.JPEG, 2);
        imgReader.setOnImageAvailableListener(imgListen, handler);

        surface = new Surface(texture);
        sList = new LinkedList<>();
        sList.add(surface);
        sList.add(imgReader.getSurface());
        openCamera();
    }


    private void openCamera() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
        }

        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String iCameraID : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(iCameraID);

                if (CameraCharacteristics.LENS_FACING_BACK == characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    cameraID = iCameraID;
                }
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }

        try {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.CAMERA)) {

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.CAMERA},
                            2);
                }
            } else {

                manager.openCamera(cameraID, devSCallback, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setupCameraPreview() {
        try {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {

                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            } else {
                capReqB = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                capReqB.addTarget(surface);
                //capReqB.addTarget(imgReader.getSurface());
                capReq = capReqB.build();
                try {
                    device.createCaptureSession(sList, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                sesh = session;
                                sesh.setRepeatingRequest(capReq, null, handler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takePicture(View view) {

        try {
            CaptureRequest.Builder captureBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imgReader.getSurface());
            //captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            sesh.stopRepeating();
            sesh.capture(captureBuilder.build(), null, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}