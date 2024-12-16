package com.dds.gles.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.dds.base.camera.CameraUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Camera2Manager {
    private static final Handler HANDLER = new LooperHandler("camera_thread", Process.THREAD_PRIORITY_DEFAULT);
    private static final String TAG = "dds_Camera2Manager";
    private final CameraClient mClient;
    private Size mPreviewSize;
    private Surface mSurface;
    private String mCameraId = "0";

    public Camera2Manager(Context context) {
        mClient = new CameraClient(context, HANDLER, new CameraOpenStateCallback());
    }

    // -------------------------public--------------------------------------

    public void initCamera(String cameraId, Size desiredPreviewSize) {
        Log.i(TAG, "initCamera: cameraId = " + mCameraId + ",size = " + desiredPreviewSize);
        mCameraId = cameraId;
        CameraCharacteristics cameraCharacteristics = mClient.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            mPreviewSize = CameraUtils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), desiredPreviewSize);
        }
    }

    public void setSurfaceFuture(Surface surface) {
        mSurface = surface;
    }

    public void openCamera() {
        if (TextUtils.isEmpty(mCameraId)) {
            Log.d(TAG, "openCamera: should initCamera first");
            return;
        }
        boolean open = mClient.openCamera(mCameraId);
        if (!open) {
            Log.e(TAG, "openCamera: no permission");
        }
    }

    public void switchCamera() {
        if (mClient != null) {
            mClient.release();
            mClient.openCamera(mCameraId);
        }
    }

    public void closeCamera() {
        if (mClient != null) {
            mClient.release();
        }
    }

    public String getNextCameraId() {
        if (mClient == null) {
            return null;
        }
        List<String> cameraIds = Arrays.asList(mClient.getCameraIds());
        if (cameraIds.size() < 2) {
            return null;
        }
        int cameraNameIndex = cameraIds.indexOf(mCameraId);
        return cameraIds.get((cameraNameIndex + 1) % cameraIds.size());
    }

    public String getDefaultCameraId() {
        String[] cameraIds = mClient.getCameraIds();
        return cameraIds[0];
    }

    public String getCurrentCameraId() {
        return mCameraId;
    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public boolean isFrontFacing() {
        CameraCharacteristics characteristics = mClient.getCameraCharacteristics(mCameraId);
        if (characteristics != null) {
            Integer value = characteristics.get(CameraCharacteristics.LENS_FACING);
            return value != null && value == CameraMetadata.LENS_FACING_FRONT;
        }
        return false;
    }

    public int getSensorRotation() {
        CameraCharacteristics characteristics = mClient.getCameraCharacteristics(mCameraId);
        if (characteristics != null) {
            Integer sensorRotation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (sensorRotation != null) {
                return sensorRotation;
            }
        }
        return 90;
    }

    public static void doOrPost(Runnable runnable) {
        if (Looper.myLooper() == HANDLER.getLooper()) {
            runnable.run();
        } else {
            HANDLER.post(runnable);
        }
    }

    // -------------------------private--------------------------------------
    public class CameraOpenStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG, "onOpened: " + cameraDevice.getId());
            mClient.setDevice(cameraDevice);
            mClient.setPreviewSurface(mSurface);
            List<Surface> list = Collections.singletonList(mSurface);
            mClient.configureStream(list);
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


}
