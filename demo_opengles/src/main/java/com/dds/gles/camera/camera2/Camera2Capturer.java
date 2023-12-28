package com.dds.gles.camera.camera2;

import android.content.Context;
import android.hardware.camera2.CameraManager;

import com.dds.gles.camera.CameraCapturer;
import com.dds.gles.camera.CameraEnumerator;

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
