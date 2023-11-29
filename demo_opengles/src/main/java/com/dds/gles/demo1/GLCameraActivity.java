package com.dds.gles.demo1;

import android.app.Activity;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.base.utils.StatueBarUtils;
import com.dds.gles.R;
import com.dds.gles.demo1.render.camera.CameraPreViewRenderer;

public class GLCameraActivity extends AppCompatActivity {
    private GLSurfaceView surfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatueBarUtils.setStatusBarOrScreenStatus(this);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}