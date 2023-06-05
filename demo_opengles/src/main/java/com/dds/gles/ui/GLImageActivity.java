package com.dds.gles.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.gles.R;
import com.dds.gles.render.camera.CameraPreViewRenderer;
import com.dds.gles.render.image.ImageRenderer;

import java.io.IOException;

public class GLImageActivity extends AppCompatActivity {
    private GLSurfaceView surfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        surfaceView = findViewById(R.id.gl_surface);
        surfaceView.setEGLContextClientVersion(3);
        // 画一张图片
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(this.getResources().getAssets().open("image.webp"));
            surfaceView.setRenderer(new ImageRenderer(bitmap));  // 展示图片渲染器

        } catch (IOException e) {
            e.printStackTrace();
        }
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