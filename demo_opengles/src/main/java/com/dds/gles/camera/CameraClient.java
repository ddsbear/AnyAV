package com.dds.gles.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;

public class CameraClient {
    private static final String TAG = "dds_CameraClient";
    private final Handler mHandler;
    private CameraDevice mDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder previewRequestBuilder;
    private final CameraDevice.StateCallback cameraStateCallback;
    private final ConfigureStateCallBack configureStateCallBack;
    private final CameraManager mCameraManager;
    private final Context mContext;
    private final PreviewRepeatingCallback repeatingCallback = new PreviewRepeatingCallback();
    private Surface previewSurface;
    private String mCameraName;


    public CameraClient(Context context, Handler handler, CameraDevice.StateCallback cameraStateCallback) {
        mHandler = handler;
        mContext = context;
        this.cameraStateCallback = cameraStateCallback;
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        configureStateCallBack = new ConfigureStateCallBack();
    }

    public boolean openCamera(String cameraName) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            try {
                mCameraName = cameraName;
                Log.d(TAG, "openCamera: " + mCameraName);
                mCameraManager.openCamera(cameraName, cameraStateCallback, mHandler);
            } catch (CameraAccessException e) {
                Log.e(TAG, "openCamera: ", e);

            }
            return true;
        }
    }

    public void configureStream(List<Surface> outputs) {
        try {
            mDevice.createCaptureSession(outputs, configureStateCallBack, mHandler);
        } catch (CameraAccessException e) {
            Log.d(TAG, "configureStream: " + e);
        }
    }

    public void startPreview() {
        try {
            if (mDevice != null && mCaptureSession != null) {
                previewRequestBuilder = mDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                previewRequestBuilder.addTarget(previewSurface);

                previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                CaptureRequest captureRequest = previewRequestBuilder.build();
                mCaptureSession.setRepeatingRequest(captureRequest, repeatingCallback, mHandler);
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "onConfigured: ", e);
        }
    }

    public void release() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mDevice != null) {
            mDevice.close();
            mDevice = null;
            Log.d(TAG, "release: " + mCameraName);
        }
    }

    public void setDevice(CameraDevice device) {
        mDevice = device;
    }

    public void setPreviewSurface(Surface previewSurface) {
        this.previewSurface = previewSurface;
    }

    public String[] getCameraIds() {
        try {
            return mCameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception", e);
            return new String[]{};
        }
    }

    public CameraCharacteristics getCameraCharacteristics(String deviceName) {
        try {
            return mCameraManager.getCameraCharacteristics(deviceName);
        } catch (CameraAccessException | RuntimeException e) {
            Log.e(TAG, "Camera access exception", e);
            return null;
        }
    }

    private class ConfigureStateCallBack extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "onConfigured: " + session.getDevice().getId());
            mCaptureSession = session;
            startPreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "onConfigureFailed: ");
            session.close();
        }
    }


}
