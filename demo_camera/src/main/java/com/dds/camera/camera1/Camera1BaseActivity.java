package com.dds.camera.camera1;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.base.utils.StatueBarUtils;
import com.dds.base.utils.Toasts;

public class Camera1BaseActivity extends AppCompatActivity {
    private static final String TAG = "Camera1BaseActivity";
    protected Button btnSwitch;
    protected Button btnPicture;
    protected CameraPresenter cameraPresenter;
    protected final Size mDesiredPreviewSize = new Size(1920, 1080);
    private OrientationLiveData orientationLiveData;
    private boolean mIsTaking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatueBarUtils.setStatusBarOrScreenStatus(this);
        orientationLiveData = new OrientationLiveData(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraPresenter != null) {
            cameraPresenter.releaseCamera();
        }
    }

    protected void initListener() {
        if (btnSwitch != null) {
            btnSwitch.setOnClickListener(v -> switchCamera());
        }
        if (btnPicture != null) {
            btnPicture.setOnClickListener(v -> takePicture());
        }

    }

    protected void takePicture() {
        cameraPresenter.setCameraCallBack(new CameraPresenter.CameraCallBack() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

            }

            @Override
            public void onTakePicture(byte[] data, Camera Camera) {
                mIsTaking = false;
            }
        });
        if (mIsTaking) {
            Log.d(TAG, "takePicture: is taking");
            return;
        }
        mIsTaking = true;
        cameraPresenter.takePicture(orientationLiveData.getValue() == null ? 90 : orientationLiveData.getValue());
    }

    protected void switchCamera() {
        cameraPresenter.switchCamera();
        Toast.makeText(this,"Turned on timer sec). Photos will be transferred to this device.", Toast.LENGTH_SHORT).show();
    }

}
