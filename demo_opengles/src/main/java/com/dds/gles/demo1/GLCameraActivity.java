package com.dds.gles.demo1;

import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.base.utils.StatueBarUtils;
import com.dds.gles.R;
import com.dds.gles.demo1.camera.Camera2Manager;
import com.dds.gles.demo1.render.CameraPreViewRenderer;
import com.dds.gles.demo2.Utils;

public class GLCameraActivity extends AppCompatActivity {
    private static final String TAG = "GLCameraActivity";
    private GLSurfaceView surfaceView;
    private Camera2Manager camera2Manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatueBarUtils.setStatusBarOrScreenStatus(this);
        if (!Utils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }
        setContentView(R.layout.activity_camera_preview);

        surfaceView = findViewById(R.id.gl_surface);
        // config version
        surfaceView.setEGLContextClientVersion(3);
        // set render
        CameraPreViewRenderer cameraPreViewRenderer = new CameraPreViewRenderer(surfaceView);
        surfaceView.setRenderer(cameraPreViewRenderer);

        // open camera
        camera2Manager = new Camera2Manager(this, cameraPreViewRenderer);
        camera2Manager.openCamera();
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
        camera2Manager.closeCamera();
    }

    public void onSwitch(View view) {
        camera2Manager.switchCamera();
    }

    public void onPicture(View view) {
    }

    public void onFilter(View view) {

    }
}