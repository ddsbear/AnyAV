package com.dds.gles.demo1.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.Collections;

public class CameraClient {
    private static final String TAG = "CameraClient";
    private String mCameraId;
    private Handler mHandler;
    private CameraDevice mDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;
    private SurfaceTexture surfaceTexture;
    private MyStateCallBack stateCallBack;

    private CameraCaptureSession mSession;

    public CameraClient(String mCameraId, Handler handler) {
        mHandler = handler;
        this.mCameraId = mCameraId;
        surfaceTexture = new SurfaceTexture(0);
        surfaceTexture.setDefaultBufferSize(1920, 1080);
        stateCallBack = new MyStateCallBack();
    }

    public boolean openCamera(Context context, CameraDevice.StateCallback cameraStateCallback, Handler cameraHandler) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            try {
                cameraManager.openCamera(mCameraId, cameraStateCallback, cameraHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    public void setDevice(CameraDevice device) {
        mDevice = device;
    }

    public void startPreview() {
        Surface previewSurface = new Surface(surfaceTexture);
        try {
            captureRequestBuilder = mDevice.createCaptureRequest(android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);
            mDevice.createCaptureSession(Collections.singletonList(previewSurface), stateCallBack, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (mDevice != null) {
            mDevice.close();
            mDevice = null;
            Log.d(TAG, "release: " + mCameraId);
        }
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }

    }

    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    private class MyStateCallBack extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            Log.d(TAG, "onConfigured: ");
            mSession = session;
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequest = captureRequestBuilder.build();
            try {
                mSession.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback() {
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

}
