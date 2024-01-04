package com.dds.gles.demo1.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

import com.dds.gles.demo1.render.CameraPreViewRenderer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Camera2Manager {

    private static final String TAG = "Camera2Manager";
    private final Context mContext;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;
    private CameraClient mClient;
    private final CameraOpenStateCallback stateCallback = new CameraOpenStateCallback();
    private final CameraPreViewRenderer mCameraPreViewRenderer;

    public Camera2Manager(Context context, CameraPreViewRenderer cameraPreViewRenderer) {
        this.mCameraPreViewRenderer = cameraPreViewRenderer;
        mContext = context;
        startCameraThread();
    }

    public void openCamera() {
        mClient = new CameraClient(mContext, mCameraHandler, stateCallback);
        String[] deviceNames = mClient.getDeviceNames(mContext);
        boolean open = mClient.openCamera(deviceNames[0]);
        if (!open) {
            Log.e(TAG, "openCamera: no permission");
        }
    }

    public void switchCamera() {
        if (mClient != null) {
            mClient.release();
            mClient.switchCamera();
        }
    }

    public void closeCamera() {
        if (mClient != null) {
            mClient.release();
        }
        stopCameraThread();
    }

    private void startCameraThread() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());

    }

    private void stopCameraThread() {
        if (mCameraThread != null) {
            mCameraThread.quitSafely();
            try {
                mCameraThread.join();
                mCameraThread = null;
                mCameraHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private class CameraOpenStateCallback extends android.hardware.camera2.CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull android.hardware.camera2.CameraDevice cameraDevice) {
            Log.d(TAG, "onOpened: ");
            mClient.setDevice(cameraDevice);
            CompletableFuture<SurfaceTexture> surfaceTextureCompletableFuture = mCameraPreViewRenderer.getCompletableFuture();
            SurfaceTexture surfaceTexture = null;
            try {
                surfaceTexture = surfaceTextureCompletableFuture.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                Log.d(TAG, "onOpened: " + e);
            }
            if (surfaceTexture == null) {
                Log.d(TAG, "onOpened: surfaceTexture == null");
                return;
            }
            surfaceTexture.setDefaultBufferSize(1920, 1080);
            mClient.startPreview(surfaceTexture);
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
