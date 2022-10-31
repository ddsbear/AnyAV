package com.dds.gles.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

public class MyManager {

    private static final String TAG = "MyManager";
    private Context mContext;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;
    private CameraStateCallback stateCallback;

    private CameraClient mClient;


    public MyManager(Context context) {
        mContext = context;
        stateCallback = new CameraStateCallback();
        startCameraThread();
    }

    public void openCamera(String id) {
        mClient = new CameraClient(id, mCameraHandler);
        boolean result = mClient.openCamera(mContext, stateCallback, mCameraHandler);
    }

    public void release() {
        stopCameraThread();
        mClient.release();
    }

    public SurfaceTexture getSurfaceTexture(){
        return mClient.getSurfaceTexture();
    }

    private void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

    }

    private void stopCameraThread() {
        mCameraThread.quitSafely();
        try {
            mCameraThread.join();
            mCameraThread = null;
            mCameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    class CameraStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "onOpened: ");
            mClient.setDevice(cameraDevice);
            mClient.startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "onDisconnected: ");

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "onError: ");
        }
    }

}
