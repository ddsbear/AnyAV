package com.dds.gles.demo2;

import android.content.Context;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;

import androidx.lifecycle.LiveData;

public class DeviceOrientationLiveData extends LiveData<Integer> {
    private static final String TAG = "OrientationLiveData";
    private final Context mContext;
    private final OrientationEventListener listener;

    public DeviceOrientationLiveData(Context context) {
        mContext = context;
        listener = new OrientationEventListener(mContext.getApplicationContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = orientation <= 45 ? Surface.ROTATION_0 :
                        (orientation <= 135 ? Surface.ROTATION_90 :
                                (orientation <= 225 ? Surface.ROTATION_180 :
                                        (orientation <= 315 ? Surface.ROTATION_270 : 0)));

                Integer relative = computeDeviceRotation(rotation);
                Integer value = getValue();
                if (!relative.equals(value)) {
                    Log.d(TAG, "onOrientationChanged: postValue " + relative);
                    postValue(relative);
                }
            }
        };
    }

    @Override
    protected void onActive() {
        listener.enable();
    }

    @Override
    protected void onInactive() {
        listener.disable();
    }

    private int computeDeviceRotation(int surfaceRotation) {
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

        return deviceOrientationDegrees;
    }
}
