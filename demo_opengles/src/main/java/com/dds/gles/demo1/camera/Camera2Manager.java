package com.dds.gles.demo1.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

public class Camera2Manager {

    private static final String TAG = "MyManager";
    private final Context mContext;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;
    private CameraClient mClient;
    private final CameraStateCallback stateCallback = new CameraStateCallback();

    public Camera2Manager(Context context) {
        mContext = context;
        startCameraThread();
    }

    public void openCamera(String id) {
        mClient = new CameraClient(id, mCameraHandler);
        mClient.openCamera(mContext, stateCallback, mCameraHandler);
    }

    public void closeCamera() {
        stopCameraThread();
        mClient.release();
    }

    public SurfaceTexture getSurfaceTexture() {
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


    private class CameraStateCallback extends android.hardware.camera2.CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull android.hardware.camera2.CameraDevice cameraDevice) {
            Log.d(TAG, "onOpened: ");
            mClient.setDevice(cameraDevice);
            mClient.startPreview();
        }

        @Override
        public void onDisconnected(@NonNull android.hardware.camera2.CameraDevice camera) {
            Log.d(TAG, "onDisconnected: ");

        }

        @Override
        public void onClosed(@NonNull android.hardware.camera2.CameraDevice camera) {
            Log.d(TAG, "onClosed: ");
        }

        @Override
        public void onError(@NonNull android.hardware.camera2.CameraDevice camera, int error) {
            Log.d(TAG, "onError: ");
        }
    }

}
