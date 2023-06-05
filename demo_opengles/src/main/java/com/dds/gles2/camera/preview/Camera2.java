package com.dds.gles2.camera.preview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;

public class Camera2 implements ICamera {

    CameraManager mCameraManager;
    private Handler mCameraOperationHandler;
    private HandlerThread mCameraOperationThread;

    public Camera2(Context context) {
        mCameraOperationThread = new HandlerThread("CameraOperationThread");
        mCameraOperationThread.start();
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        mCameraOperationHandler = new Handler(mCameraOperationThread.getLooper());
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean open(int cameraId) {
        try {
            mCameraManager.openCamera(String.valueOf(cameraId),null,mCameraOperationHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean preview() {
        return false;
    }

    @Override
    public boolean close() {
        return false;
    }

    @Override
    public void setPreviewTexture(SurfaceTexture texture) {

    }

    @Override
    public Point getPreviewSize() {
        return null;
    }
}
