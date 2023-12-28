package com.dds.gles.demo1;

import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.base.utils.StatueBarUtils;
import com.dds.gles.R;
import com.dds.gles.demo1.render.camera.CameraPreViewRenderer;
import com.dds.gles.demo2.Utils;
import com.dds.gles.demo2.render.GLESTool;

public class GLCameraActivity extends AppCompatActivity {
    private GLSurfaceView surfaceView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatueBarUtils.setStatusBarOrScreenStatus(this);
        if (!Utils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }
        setContentView(R.layout.activity_camera_preview);
        surfaceView = findViewById(R.id.gl_surface);
        surfaceView.setEGLContextClientVersion(3);
        // 摄像头纹理
        surfaceView.setRenderer(new CameraPreViewRenderer(this));

        String a = "0.6f";
        Float.parseFloat(a);


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


    public void onSwitch(View view) {
        Thread thread = new Thread(() -> {
            while (true) {
                String a = "0.6";
                float v = Float.parseFloat(a);
                Log.e("dds_test", String.format("v = %8f", v));
                Log.e("dds_test", String.valueOf(v));
                assert v == 0.6f;
            }
        });
        thread.start();
    }

    public void onPicture(View view) {
    }
}