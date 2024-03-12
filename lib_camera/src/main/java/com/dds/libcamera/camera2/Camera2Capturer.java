package com.dds.libcamera.camera2;

import android.content.Context;
import android.hardware.camera2.CameraManager;

import com.dds.libcamera.CameraCapturer;
import com.dds.libcamera.CameraEnumerator;

public class Camera2Capturer extends CameraCapturer {

    private final CameraManager cameraManager;

    public Camera2Capturer(Context context, String cameraName, CameraEnumerator enumerator) {
        super(cameraName, enumerator);
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    protected void createCameraSession() {

    }
}
