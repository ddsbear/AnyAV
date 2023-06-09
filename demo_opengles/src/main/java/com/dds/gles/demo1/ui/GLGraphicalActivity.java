package com.dds.gles.demo1.ui;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.gles.R;
import com.dds.gles.demo1.render.triangle.TriangleRenderer;

public class GLGraphicalActivity extends AppCompatActivity {
    private GLSurfaceView surfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        surfaceView = findViewById(R.id.gl_surface);
        surfaceView.setEGLContextClientVersion(3);

        // 绘制图形
        surfaceView.setRenderer(new TriangleRenderer());

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