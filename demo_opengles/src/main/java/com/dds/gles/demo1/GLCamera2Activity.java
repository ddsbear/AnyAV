package com.dds.gles.demo1;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dds.base.camera.CameraUtils;
import com.dds.base.utils.StatueBarUtils;
import com.dds.base.utils.Toasts;
import com.dds.gles.R;
import com.dds.gles.camera.Camera2Manager;
import com.dds.gles.camera.LooperHandler;
import com.dds.gles.demo1.render.CameraPreViewRenderer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GLCamera2Activity extends AppCompatActivity {
    private static final String TAG = "dds_GLCameraActivity@";
    private GLSurfaceView surfaceView;
    private final Size mDesiredPreviewSize = new Size(1280, 720);
    private Camera2Manager mCamera2Manager;
    private CameraPreViewRenderer cameraPreViewRenderer;

    private boolean enableFilter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatueBarUtils.setStatusBarOrScreenStatus(this);
        setContentView(R.layout.activity_gl_camera2);
        surfaceView = findViewById(R.id.gl_surface);
        // config version
        surfaceView.setEGLContextClientVersion(3);
        // set render
        cameraPreViewRenderer = new CameraPreViewRenderer(mDesiredPreviewSize);
        surfaceView.setRenderer(cameraPreViewRenderer);
        // resize
        resizeSurfaceView(surfaceView);
        // camera
        mCamera2Manager = new Camera2Manager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        Camera2Manager.doOrPost(() -> {
            // open camera
            String defaultCameraId = mCamera2Manager.getDefaultCameraId();
            mCamera2Manager.initCamera(defaultCameraId, mDesiredPreviewSize);
            configOutPutSurface(mCamera2Manager);
            mCamera2Manager.openCamera();
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
        if (mCamera2Manager != null) {
            mCamera2Manager.closeCamera();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        if (mCamera2Manager != null) {
            mCamera2Manager.closeCamera();
        }
        if (cameraPreViewRenderer != null) {
            cameraPreViewRenderer.release();
        }
    }

    public void onSwitch(View view) {
        String nextCameraId = mCamera2Manager.getNextCameraId();
        if (nextCameraId != null) {
            mCamera2Manager.initCamera(nextCameraId, mDesiredPreviewSize);
            configOutPutSurface(mCamera2Manager);
            mCamera2Manager.switchCamera();
        }

    }

    public void onPicture(View view) {
        Toasts.show(this, "not support yet", Toast.LENGTH_SHORT);
    }

    public void onFilter(View view) {
        cameraPreViewRenderer.enableFilter(enableFilter = !enableFilter);
    }

    private void resizeSurfaceView(View surfaceView) {
        Size layoutSize = CameraUtils.findBestLayoutSize(this, mDesiredPreviewSize);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
        params.width = layoutSize.getWidth();
        params.height = layoutSize.getHeight();
        params.gravity = Gravity.CENTER;
    }

    private void configOutPutSurface(Camera2Manager camera2Manager) {
        CompletableFuture<SurfaceTexture> completableFuture = cameraPreViewRenderer.getSurfaceFuture();
        SurfaceTexture surface = null;
        try {
            surface = completableFuture.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.d(TAG, "future.get: " + e);
        }
        if (surface == null) {
            Log.d(TAG, "surfaceTexture == null");
            return;
        }
        camera2Manager.setSurfaceFuture(new Surface(surface));

        cameraPreViewRenderer.setFront(mCamera2Manager.isFrontFacing());

    }


}