package com.dds.camera.camera1;

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

    public OrientationLiveData(Context context) {
        mContext = context;
        listener = new OrientationEventListener(mContext.getApplicationContext()) {
            @Override
            public void onOrientationChanged(int orientation) {
                int rotation = orientation <= 45 ? 90 :
                        (orientation <= 135 ? 180 :
                                (orientation <= 225 ? 270 :
                                        (orientation <= 315 ? 0
                                                : 90)));

                postValue(rotation);
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

}
