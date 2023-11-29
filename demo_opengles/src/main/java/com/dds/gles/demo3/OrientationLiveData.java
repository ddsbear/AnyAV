package com.dds.gles.demo3;

import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;

import androidx.lifecycle.LiveData;

public class OrientationLiveData extends LiveData<Integer> {
    private static final String TAG = "OrientationLiveData";
    private final Context mContext;
    private final OrientationEventListener listener;

    public OrientationLiveData(Context context, CameraCharacteristics characteristics) {
        mContext = context;
        listener = new OrientationEventListener(mContext.getApplicationContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = orientation <= 45 ? Surface.ROTATION_0 :
                        (orientation <= 135 ? Surface.ROTATION_90 :
                                (orientation <= 225 ? Surface.ROTATION_180 :
                                        (orientation <= 315 ? Surface.ROTATION_270 : 0)));

                Integer relative = computeRelativeRotation(characteristics, rotation);
                Integer value = getValue();
                boolean isChange = true;
                if (value != null) {
                    int dist = Math.abs(relative - value);
                    dist = Math.min(dist, 360 - dist);
                    isChange = dist >= 68;
                }
                if (!relative.equals(value) && isChange) {
                    Log.d(TAG, "onOrientationChanged: postValue " + relative);
                    postValue(relative);
                }
            }
        };
    }

    @Override
    protected void onActive() {
        super.onActive();
        listener.enable();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        listener.disable();
    }

    private int computeRelativeRotation(CameraCharacteristics characteristics, int surfaceRotation) {
        Integer integer = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        int sensorOrientationDegrees = integer == null ? 0 : integer;
        int deviceOrientationDegrees = 0;
        switch (surfaceRotation) {
            case Surface.ROTATION_90:
                deviceOrientationDegrees = 90;
                break;
            case Surface.ROTATION_180:
                deviceOrientationDegrees = 180;
                break;
            case Surface.ROTATION_270:
                deviceOrientationDegrees = 270;
                break;
        }
        Integer integerLens = characteristics.get(CameraCharacteristics.LENS_FACING);
        int sign = 1;
        if (integerLens != null) {
            sign = integerLens == CameraCharacteristics.LENS_FACING_FRONT ? 1 : -1;
        }
        return (sensorOrientationDegrees - (deviceOrientationDegrees * sign) + 360) % 360;
    }
}
