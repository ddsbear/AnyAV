package com.dds.gles;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.gles.render.camera.CameraPreViewRenderer;

public class OpenGLActivity extends AppCompatActivity {
    private GLSurfaceView surfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_glactivity);
        surfaceView = findViewById(R.id.gl_surface);
        surfaceView.setEGLContextClientVersion(3);
//        surfaceView.setRenderer(new TriangleRenderer());

//        try {
//            Bitmap bitmap = BitmapFactory.decodeStream(this.getResources().getAssets().open("image.webp"));
//            surfaceView.setRenderer(new ImageRenderer(bitmap));  // 展示图片渲染器
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

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