package com.dds.gles.ui;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.gles.R;
import com.dds.gles.render.camera.CameraPreViewRenderer;

public class GLCameraActivity extends AppCompatActivity {
    private GLSurfaceView surfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        surfaceView = findViewById(R.id.gl_surface);
        surfaceView.setEGLContextClientVersion(3);

        // 摄像头纹理
        surfaceView.setRenderer(new CameraPreViewRenderer(this));

    }

    @Override
    protected void onPause() {
        super.onPause();
        surfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceView.onResume();
    }
}