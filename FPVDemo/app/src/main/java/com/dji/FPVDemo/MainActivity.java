/*
    This program uses the previously collected information
    to move the drone in a lawn mower pattern.
 */
package com.dji.FPVDemo;
import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJIError;
import dji.common.flightcontroller.virtualstick.FlightControlData;
import dji.common.flightcontroller.virtualstick.FlightCoordinateSystem;
import dji.common.flightcontroller.virtualstick.RollPitchControlMode;
import dji.common.flightcontroller.virtualstick.VerticalControlMode;
import dji.common.gimbal.CapabilityKey;
import dji.common.gimbal.Rotation;
import dji.common.gimbal.RotationMode;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.common.util.DJIParamCapability;
import dji.common.util.DJIParamMinMaxCapability;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.gimbal.Gimbal;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import static com.dji.FPVDemo.FPVDemoApplication.getProductInstance;
public class MainActivity extends Activity implements SurfaceTextureListener, OnClickListener{
    private static final String TAG = MainActivity.class.getName();
    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;
    // Codec for video live view
    protected DJICodecManager mCodecManager = null;
    // d is short for drone
    private float dWidth, dLength, dVelocity, dAltitude, dPassSpacing, testIterator;
    private String dTransverseAxis, dCameraOrientation;
    protected TextureView mVideoSurface = null;
    private Button mInitDroneBtn, mLineBtn;
    private TextView recordingTime;
    private final Camera camera2 = FPVDemoApplication.getCameraInstance();
    Aircraft phanthom4 = (Aircraft) getProductInstance();
    public FlightController flightController = phanthom4.getFlightController();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle values = getIntent().getExtras();

        dWidth = values.getFloat("width");
        dLength = values.getFloat("length");
        dVelocity = values.getFloat("velocity");
        dAltitude = values.getFloat("altitude");
        dPassSpacing = values.getFloat("passSpacing");
        dTransverseAxis = values.getString("transverseAxis");
        dCameraOrientation = values.getString("cameraOrientation");

        initUI();
        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {
            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };
        if (camera2 != null) {
        }
    }
    protected void onProductChange() {
        initPreviewer();
    }
    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();
        if(mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }
    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }
    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }
    public void onReturn(View view){
        Log.e(TAG, "onReturn");
        this.finish();
    }
    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        super.onDestroy();
    }
    private void initUI() {
        // init mVideoSurface
        mVideoSurface = (TextureView)findViewById(R.id.video_previewer_surface);
        recordingTime = (TextView) findViewById(R.id.timer);
        mInitDroneBtn = (Button) findViewById(R.id.initDrone);
        mLineBtn = (Button) findViewById(R.id.goButton);
        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);
        }
        mInitDroneBtn.setOnClickListener(this);
        mLineBtn.setOnClickListener(this);
        recordingTime.setVisibility(View.INVISIBLE);
    }
    private void initPreviewer() {
        BaseProduct product = FPVDemoApplication.getProductInstance();
        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                if (VideoFeeder.getInstance().getVideoFeeds() != null
                        && VideoFeeder.getInstance().getVideoFeeds().size() > 0) {
                    VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(mReceivedVideoDataCallBack);
                }
            }
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.initDrone:{
                SettingsDefinitions.CameraMode cameraMode = SettingsDefinitions.CameraMode.SHOOT_PHOTO;
                camera2.setMode(cameraMode, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {

                        SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.INTERVAL; // Set the camera capture mode as Interval mode
                        camera2.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (null == djiError) {

                                    SettingsDefinitions.PhotoTimeIntervalSettings photoTimeIntervalSettings = new SettingsDefinitions.PhotoTimeIntervalSettings(255, 2);
                                    camera2.setPhotoTimeIntervalSettings(photoTimeIntervalSettings, new CommonCallbacks.CompletionCallback() {
                                        public void onResult(DJIError djiError) {
                                            if (djiError == null) {

                                                initDroneMethod();
                                            }
                                        }
                                    });
                                } else {
                                    showToast("Camera incorrectly configured. Try restarting your drone.");
                                }
                            }
                        });
                    }
                });
                break;
            }
            case R.id.goButton:{

                flyLawnmower(dWidth, dLength, dVelocity, dAltitude, dPassSpacing, dTransverseAxis);
            }
            default:
                break;
        }
    }

    // This function flys the drone in a lawn mower pattern
    public void flyLawnmower(final float width, final float length,final  float velocity,final  float altitude,final  float passSpacing, String transverseAxis){
        transverseAxis = transverseAxis.toLowerCase();
        if(dCameraOrientation.equals("forward")){
            rotateGimbalForward();
        }
        else if(dCameraOrientation.equals("down")){
            rotateGimbalDown();
        }

        if (transverseAxis.equals("width")){
            testIterator = 0;
            double altitudeOffset = .15;
            if(altitude > flightController.getState().getAircraftLocation().getAltitude()) {
                while (flightController.getState().getAircraftLocation().getAltitude() < (altitude - altitudeOffset)) {
                    flightController.sendVirtualStickFlightControlData(
                            new FlightControlData(
                                    0, 0, 0, altitude
                            ), new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                    } else {
                                    }
                                }
                            }
                    );
                }
            }
            else{
                while (flightController.getState().getAircraftLocation().getAltitude() > (altitude + altitudeOffset)) {
                    flightController.sendVirtualStickFlightControlData(
                            new FlightControlData(
                                    0, 0, 0, altitude
                            ), new CommonCallbacks.CompletionCallback() {
                                @Override
                                public void onResult(DJIError djiError) {
                                    if (djiError != null) {
                                    } else {
                                    }
                                }
                            }
                    );
                }
            }
            camera2.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        showToast("Pictures have started to be taken on an interval.");
                        int numPasses = (int) Math.ceil(length / passSpacing);
                        String lateralDirectionWidth = "E";
                        quadLine(lateralDirectionWidth, velocity, width, altitude);
                        stop(altitude);
                        for(float x = 0; x < numPasses; x++){
                            quadLine("N", velocity, passSpacing, altitude);
                            stop(altitude);
                            if(lateralDirectionWidth.equals("E")){
                                lateralDirectionWidth = "W";
                            }
                            else if(lateralDirectionWidth.equals("W")){
                                lateralDirectionWidth = "E";
                            }
                            else{
                            }
                            quadLine(lateralDirectionWidth, velocity, width, altitude);
                            stop(altitude);
                        }
                        camera2.stopShootPhoto(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                showToast("Flight is over and pictures taken.");
                                DJISDKManager.getInstance().archiveLogs(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        showToast("Archived Logs");
                                    }
                                });
                            }
                        });
                    } else {
                        showToast("Error occured when attempting to take pictures. Ensure the SD card has enough space and restart your drone");
                    }
                }
            });
        }
        else if(transverseAxis.equals("length")){
            camera2.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        showToast("Pictures have started to be taken on an interval.");
                        int numPasses = (int) Math.ceil(length / passSpacing);

                        String lateralDirection = "N";
                        quadLine(lateralDirection, velocity, width, altitude);
                        stop(altitude);
                        for(float x = 0; x < numPasses; x++){
                            quadLine("E", velocity, passSpacing, altitude);
                            stop(altitude);
                            if(lateralDirection.equals("N")){
                                lateralDirection = "S";
                            }
                            else if(lateralDirection.equals("S")){
                                lateralDirection = "N";
                            }
                            else{
                            }
                            quadLine(lateralDirection, velocity, width, altitude);
                            stop(altitude);
                        }
                        camera2.stopShootPhoto(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {

                                showToast("Flight is over and pictures taken.");
                                DJISDKManager.getInstance().archiveLogs(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        showToast("Archived Logs");
                                    }
                                });
                            }
                        });
                    } else {
                        showToast("Error occured when attempting to take pictures. Ensure the SD card has enough space and restart your drone");
                    }
                }
            });
        }
        else{
            showToast("Incorrect transverse axis");
        }
    }

    // This function starts the takeoff procedure of the drone and configures the flight controller
    public void initDroneMethod(){

        FlightAssistant flightAssistant = flightController.getFlightAssistant();
        flightAssistant.setCollisionAvoidanceEnabled(true, new CommonCallbacks.CompletionCallback(){
            public void onResult(DJIError djiError){
            }
        });
        flightController.startTakeoff(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if(djiError == null){
                    long tenSeconds = 10000;
                    long delay = System.currentTimeMillis() + tenSeconds;
                    while(delay > System.currentTimeMillis()){
                    }
                    flightController.setVirtualStickModeEnabled(true, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                flightController.setVirtualStickAdvancedModeEnabled(true);
                                flightController.setRollPitchControlMode(RollPitchControlMode.VELOCITY);
                                flightController.setVerticalControlMode(VerticalControlMode.POSITION);
                                flightController.setRollPitchCoordinateSystem(FlightCoordinateSystem.BODY);

                                showToast("Ready for flight!");
                            }
                            else{
                                showToast("Error in configuring the drone. Try restarting the drone and try again.");
                            }
                        }
                    });
                }
                else{
                }
            }
        });
    }

    // This function is what acutally sends the flight commands to the drone
    public void quadLine(String dir, float velocity, float distance, float altitude) {
        float linePitch = 0;
        float lineRoll = 0;
        dir = dir.toUpperCase();
        if (dir.equals("N")) {
            linePitch = (1 * velocity);
        } else if (dir.equals("S")) {
            linePitch = (-1 * velocity);
        } else if (dir.equals("W")) {
            lineRoll = (-1 * velocity);
        } else if (dir.equals("E")) {
            lineRoll = (1 * velocity);
        }

        final FlightControlData flightControlDataQuadLineFunction = new FlightControlData(
                lineRoll, linePitch, 0, altitude
        );

        final long timeToRun = System.currentTimeMillis() + ((long) (distance / velocity) * 1000);
        while (timeToRun > System.currentTimeMillis()) {
            flightController.sendVirtualStickFlightControlData(
                    flightControlDataQuadLineFunction, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                        }
                    }
            );
        }
    }

    // This function is sent to the drone to stop it
    public void stop(float altitude){
        final FlightControlData flightControlDataStopFunction = new FlightControlData(
                0, 0, 0, altitude
        );
        final long timeToRun = System.currentTimeMillis() + 1000;
        while (timeToRun > System.currentTimeMillis()) {
            flightController.sendVirtualStickFlightControlData(
                    flightControlDataStopFunction, new CommonCallbacks.CompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                        }
                    }
            );
        }
    }

    private void uninitPreviewer() {
        if (camera2 != null){
            VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(null);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(this, surface, width, height);
        }
    }


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e(TAG,"onSurfaceTextureDestroyed");
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }
        return false;
    }

    // Function for popping up messages to the user
    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Function for getting the gimbal instance
    private Gimbal getGimbalInstance() {
        Gimbal gimbal = phanthom4.getGimbal();
        if (gimbal == null) {
            if (DJISDKManager.getInstance() != null) {
                BaseProduct product = DJISDKManager.getInstance().getProduct();
                if (product != null) {
                    gimbal = product.getGimbal();
                }
            }
        }
        return gimbal;
    }

    // Function used to send the command the gimbal
    private void sendRotateGimbalCommand(Rotation rotation) {
        Gimbal gimbal = getGimbalInstance();
        if (gimbal == null) {
            return;
        }
        gimbal.rotate(rotation, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError djiError) {
                if (djiError == null) {
                } else {
                }
            }
        });
    }

    // Creates command for rotating the gimbal down
    private void rotateGimbalDown() {
        Gimbal gimbal = getGimbalInstance();
        if (gimbal == null) {
            return;
        }
        Object key = CapabilityKey.ADJUST_PITCH;
        Number minValue = ((DJIParamMinMaxCapability) (gimbal.getCapabilities().get(key))).getMin();
        Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(.1);
        if (key == CapabilityKey.ADJUST_PITCH) {
            builder.pitch(minValue.floatValue());
        } else if (key == CapabilityKey.ADJUST_YAW) {
            builder.yaw(minValue.floatValue());
        } else if (key == CapabilityKey.ADJUST_ROLL) {
            builder.roll(minValue.floatValue());
        }
        sendRotateGimbalCommand(builder.build());
    }

    // Creates command for rotating the gimbal forward
    private void rotateGimbalForward() {
        Gimbal gimbal = getGimbalInstance();
        if (gimbal == null) {
            return;
        }
        Object key = CapabilityKey.ADJUST_PITCH;
        Number maxValue = ((DJIParamMinMaxCapability) (gimbal.getCapabilities().get(key))).getMax();
        Rotation.Builder builder = new Rotation.Builder().mode(RotationMode.ABSOLUTE_ANGLE).time(.1);
        if (key == CapabilityKey.ADJUST_PITCH) {
            builder.pitch(0);
        } else if (key == CapabilityKey.ADJUST_YAW) {
            builder.yaw(maxValue.floatValue());
        } else if (key == CapabilityKey.ADJUST_ROLL) {
            builder.roll(maxValue.floatValue());
        }
        sendRotateGimbalCommand(builder.build());
    }

}
