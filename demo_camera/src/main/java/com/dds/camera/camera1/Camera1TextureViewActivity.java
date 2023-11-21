package com.dds.camera.camera1;

import android.os.Bundle;
import android.view.TextureView;

import com.dds.fbo.R;

public class Camera1TextureViewActivity extends Camera1BaseActivity {

    private TextureView mTextureView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera1_texture_view);
        mTextureView = findViewById(R.id.preview_texture);
        btnSwitch = findViewById(R.id.btn_switch);
        btnPicture = findViewById(R.id.btn_take_picture);
        cameraPresenter = new CameraPresenter(this, mTextureView, mDesiredPreviewSize);
        initListener();
    }
}