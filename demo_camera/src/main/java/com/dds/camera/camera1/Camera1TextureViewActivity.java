package com.dds.camera.camera1;

import android.os.Bundle;
import android.util.Size;
import android.view.TextureView;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.base.utils.Utils;
import com.dds.fbo.R;

public class Camera1TextureViewActivity extends AppCompatActivity {

    private TextureView mTextureView;
    private CameraPresenter cameraPresenter;

    private final Size mDesiredPreviewSize = new Size(1920, 1080);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setStatusBarOrScreenStatus(this);
        setContentView(R.layout.activity_camera1_texture_view);
        mTextureView = findViewById(R.id.preview_texture);

        cameraPresenter = new CameraPresenter(this, mTextureView, mDesiredPreviewSize);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraPresenter != null) {
            cameraPresenter.releaseCamera();
        }
    }

}