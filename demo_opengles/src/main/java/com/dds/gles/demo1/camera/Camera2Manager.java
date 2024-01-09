package com.dds.gles.demo1.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;

import com.dds.base.camera.CameraUtils;
import com.dds.gles.demo1.render.CameraPreViewRenderer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Camera2Manager {

    private static final String TAG = "Camera2Manager";
    private final Context mContext;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;
    private CameraClient mClient;
    private final CameraOpenStateCallback stateCallback = new CameraOpenStateCallback();
    private CameraManager mCameraManager;
    private Size mDesiredPreviewSize;
    private Size mPreviewSize;
    private CameraPreViewRenderer cameraPreViewRenderer;


    public Camera2Manager(Context context, Size desiredPreviewSize) {
        mContext = context;
        mDesiredPreviewSize = desiredPreviewSize;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        startCameraThread();
    }

    public void openCamera() {
        mClient = new CameraClient(mContext, mCameraManager, mCameraHandler, stateCallback);
        String[] deviceNames = mClient.getDeviceNames();
        String deviceName = deviceNames[1];
        CameraCharacteristics cameraCharacteristics = getCameraCharacteristics(deviceName);
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            mPreviewSize = CameraUtils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), mDesiredPreviewSize);
            cameraPreViewRenderer = new CameraPreViewRenderer(mPreviewSize);
            boolean open = mClient.openCamera(deviceName);
            if (!open) {
                Log.e(TAG, "openCamera: no permission");
            }
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

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public CameraPreViewRenderer getCameraPreViewRenderer() {
        return cameraPreViewRenderer;
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

    private class CameraOpenStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "onOpened: ");
            mClient.setDevice(cameraDevice);
            CompletableFuture<SurfaceTexture> surfaceTextureCompletableFuture = cameraPreViewRenderer.getCompletableFuture();
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
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "onDisconnected: ");

        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {
            Log.d(TAG, "onClosed: ");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "onError: ");
        }
    }

    public boolean isFrontFacing(String deviceName) {
        CameraCharacteristics characteristics = getCameraCharacteristics(deviceName);
        if (characteristics != null) {
            Integer value = characteristics.get(CameraCharacteristics.LENS_FACING);
            return value != null && value == CameraMetadata.LENS_FACING_FRONT;
        }
        return false;
    }

    private CameraCharacteristics getCameraCharacteristics(String deviceName) {
        try {
            return mCameraManager.getCameraCharacteristics(deviceName);
        } catch (CameraAccessException | RuntimeException e) {
            Log.e(TAG, "Camera access exception", e);
            return null;
        }
    }


}
