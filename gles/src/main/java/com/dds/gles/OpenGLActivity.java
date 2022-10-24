package com.dds.gles;

import androidx.appcompat.app.AppCompatActivity;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.dds.gles.render.triangle.TriangleRenderer;

public class OpenGLActivity extends AppCompatActivity {
    private GLSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_glactivity);
        surfaceView = findViewById(R.id.gl_surface);

        surfaceView.setEGLContextClientVersion(3);

        surfaceView.setRenderer(new TriangleRenderer());

        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

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