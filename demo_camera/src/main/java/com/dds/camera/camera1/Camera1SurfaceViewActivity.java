package com.dds.camera.camera1;


import android.os.Bundle;
import android.view.SurfaceView;

import com.dds.fbo.R;

public class Camera1SurfaceViewActivity extends Camera1BaseActivity {
    private static final String TAG = "Camera1SurfaceViewActiv";
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera1_surface_view);
        mSurfaceView = findViewById(R.id.preview_surface);
        btnSwitch = findViewById(R.id.btn_switch);
        btnPicture = findViewById(R.id.btn_take_picture);
        cameraPresenter = new CameraPresenter(this, mSurfaceView, mDesiredPreviewSize);
        initListener();
    }

}