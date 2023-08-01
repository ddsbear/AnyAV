package com.dds.camera.camera1;


import android.os.Bundle;
import android.util.Size;
import android.view.SurfaceView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.base.utils.Utils;
import com.dds.fbo.R;

public class Camera1SurfaceViewActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private CameraPresenter cameraPresenter;

    private Button btnSwitch;
    private Button btnPicture;


    private OrientationLiveData orientationLiveData;

    private final Size mDesiredPreviewSize = new Size(1920, 1080);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setStatusBarOrScreenStatus(this);
        setContentView(R.layout.activity_camera1_surface_view);
        mSurfaceView = findViewById(R.id.preview_surface);
        cameraPresenter = new CameraPresenter(this, mSurfaceView, mDesiredPreviewSize);
        btnSwitch = findViewById(R.id.btn_switch);
        btnPicture = findViewById(R.id.btn_take_picture);

        initListener();
        orientationLiveData = new OrientationLiveData(this);


    }

    private void initListener() {
        btnSwitch.setOnClickListener(v -> switchCamera());
        btnPicture.setOnClickListener(v -> takePicture());

    }

    private void takePicture() {
        cameraPresenter.takePicture(orientationLiveData.getValue() == null ? 90 : orientationLiveData.getValue());
    }

    private void switchCamera() {
        cameraPresenter.switchCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraPresenter != null) {
            cameraPresenter.releaseCamera();
        }
    }


}