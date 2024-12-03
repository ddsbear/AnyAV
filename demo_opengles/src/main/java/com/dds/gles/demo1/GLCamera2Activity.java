package com.dds.gles.demo1;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Size;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.base.camera.CameraUtils;
import com.dds.base.utils.StatueBarUtils;
import com.dds.base.utils.Toasts;
import com.dds.gles.R;
import com.dds.gles.demo1.camera.Camera2Manager;

public class GLCamera2Activity extends AppCompatActivity {
    private static final String TAG = "GLCameraActivity";
    private GLSurfaceView surfaceView;
    private Camera2Manager camera2Manager;
    private final Size mDesiredPreviewSize = new Size(1920, 1080);
    private boolean enableFilter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatueBarUtils.setStatusBarOrScreenStatus(this);
//        if (!Utils.isTablet(this)) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
//        }
        setContentView(R.layout.activity_gl_camera2);
        surfaceView = findViewById(R.id.gl_surface);
        updateView(surfaceView);
        // config version
        surfaceView.setEGLContextClientVersion(3);
        // open camera
        camera2Manager = new Camera2Manager(this, mDesiredPreviewSize);


        camera2Manager.openCamera();
        // set render
        surfaceView.setRenderer(camera2Manager.getCameraPreViewRenderer());


    }


    private void updateView(View surfaceView) {
        Size layoutSize = CameraUtils.findBestLayoutSize(this, mDesiredPreviewSize);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
        params.width = layoutSize.getWidth();
        params.height = layoutSize.getHeight();
        params.gravity = Gravity.CENTER;
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
        Toasts.show(this, "not support yet", Toast.LENGTH_SHORT);
    }

    public void onFilter(View view) {
        camera2Manager.enableFilter(enableFilter = !enableFilter);
    }
}