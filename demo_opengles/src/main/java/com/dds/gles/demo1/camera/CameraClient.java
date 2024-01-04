package com.dds.gles.demo1.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CameraClient {
    private static final String TAG = "CameraClient";
    private final Handler mHandler;
    private CameraDevice mDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraDevice.StateCallback cameraStateCallback;
    private final ConfigureStateCallBack configureStateCallBack;
    private CameraManager mCameraManager;
    private String mCameraName;
    private Context mContext;


    public CameraClient(Context context, Handler handler, CameraDevice.StateCallback cameraStateCallback) {
        mHandler = handler;
        mContext = context;
        configureStateCallBack = new ConfigureStateCallBack();
        this.cameraStateCallback = cameraStateCallback;
    }

    public boolean openCamera(String cameraName) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            try {
                mCameraName = cameraName;
                Log.d(TAG, "openCamera: " + mCameraName);
                getCameraManager(mContext).openCamera(mCameraName, cameraStateCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    public void switchCamera() {

        List<String> deviceNames = Arrays.asList(getDeviceNames(mContext));
        if (deviceNames.size() < 2) {
            return;
        }
        int cameraNameIndex = deviceNames.indexOf(mCameraName);
        String cameraName = deviceNames.get((cameraNameIndex + 1) % deviceNames.size());
        openCamera(cameraName);
    }

    public void setDevice(CameraDevice device) {
        mDevice = device;
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        Surface previewSurface = new Surface(surfaceTexture);
        try {
            captureRequestBuilder = mDevice.createCaptureRequest(android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);
            mDevice.createCaptureSession(Collections.singletonList(previewSurface), configureStateCallBack, mHandler);
        } catch (CameraAccessException e) {
            Log.d(TAG, "startPreview: " + e);
        }
    }

    public void release() {
        if (mDevice != null) {
            mDevice.close();
            mDevice = null;
            Log.d(TAG, "release: " + mCameraName);
        }
    }

    private class ConfigureStateCallBack extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "onConfigured: ");
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            CaptureRequest captureRequest = captureRequestBuilder.build();
            try {
                session.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                    }
                }, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "onConfigureFailed: ");
        }
    }

    private CameraManager getCameraManager(Context context) {
        if (mCameraManager == null) {
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
        return mCameraManager;
    }

    public boolean isFrontFacing(String deviceName) {
        CameraCharacteristics characteristics = getCameraCharacteristics(deviceName);
        if (characteristics != null) {
            Integer value = characteristics.get(CameraCharacteristics.LENS_FACING);
            return value != null && value == CameraMetadata.LENS_FACING_FRONT;
        }
        return false;
    }

    public String[] getDeviceNames(Context context) {
        try {
            return getCameraManager(context).getCameraIdList();
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception", e);
            return new String[]{};
        }
    }

    private CameraCharacteristics getCameraCharacteristics(String deviceName) {
        try {
            return getCameraManager(mContext).getCameraCharacteristics(deviceName);
        } catch (CameraAccessException | RuntimeException e) {
            Log.e(TAG, "Camera access exception", e);
            return null;
        }
    }
}
