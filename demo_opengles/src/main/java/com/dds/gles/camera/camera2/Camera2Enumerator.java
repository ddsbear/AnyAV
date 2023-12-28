package com.dds.gles.camera.camera2;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dds.gles.camera.CameraEnumerator;

public class Camera2Enumerator implements CameraEnumerator {
    private static final String TAG = "Camera2Enumerator";
    final Context context;
    final CameraManager cameraManager;

    public Camera2Enumerator(Context context) {
        this.context = context;
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @Override
    public String[] getDeviceNames() {
        try {
            return cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception", e);
            return new String[]{};
        }
    }

    @Override
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
            return cameraManager.getCameraCharacteristics(deviceName);
        } catch (CameraAccessException | RuntimeException e) {
            Log.e(TAG, "Camera access exception", e);
            return null;
        }
    }
}
